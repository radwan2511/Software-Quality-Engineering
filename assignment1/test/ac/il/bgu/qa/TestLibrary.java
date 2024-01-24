package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/*
Hello Dear assignment evaluator, please read the following short introduction in order to understand our way of completing the provided Assignment.

--- SQE HW1 ---
by Radwan Ghanem and Abed Shogan
IDS: 322509951 ,212112106

   --- UNIT TESTING Using Class TestLibrary ---
   Units - Classes (Library class in this instance)
   Sub Units - Methods (Methods of the Library class)
   Tools used: JUnit 5, Mockito
   *** Format of Unit Test methods signature: Given<condition>_When<method>_Then<result>()
   *** Note: Use Annotations correctly S.A: @Test, @BeforeEach ( in setUp() ), @AfterEach( in tearDown() ), @AfterAll ( in tearDownAll() ),@ParameterizedTest,@ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE})
         Use Methods: assertEquals() etc...

    Running Tests: use the "mvn clean test" method provided with JAVA 17 or higher and the latest Maven ver.

    Submission GuideLines: Submit TestLibrary.java through the Moodle using the VPL Server until 25.01.2024

   INTRODUCTION:
   We shall perform Unit Testing  of this given Library Management System in this class, as instructed
   we will ONLY Edit the TestLibrary class, We wont change any other given class.

   Our task is to create comprehensive unit tests for the Library class using the JUnit5 FrameWork and Mockito to Isolate the Library class.
   For these purposes, we shall use Mocks,Stubs  in our testing procedures.
   MOCKS vs STUBS:
   Mocks shall be used for Behavioral Verification Testing, Stubs shall be used for State testing.
   There may be several stubs in 1 test but usually there is only one mock.

   Mock Usage: * Verify proper methods of class were activated
                   * Assert returned results (if returned) Into

   Stub Usage: Isolate code from Library, using a stub

    MOCKS:
    mockDatabaseService, mockReviewService
    mockBook, mockUser
    STUBS:
    stubNotificationService

  GOALS:
  1) Decide what to test and how to test
  2) Isolate tested classes
  3) use Mockito for the Stub/Mock tactics (Insert Mutations as well) to isolate the Library class.
  4) Test coverage of the Class and methods (Cover simple cases and extreme end cases)
  Note: the AAA testing pattern shall be used to perform the testing.
  */
public class TestLibrary {
    // mocked external services / Dependencies

    // to check System.out.println() outputs from class Library
    private final PrintStream originalSystemOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    // to check System.err.println() outputs from class Library
    private final PrintStream originalSystemErr = System.err;
    private final ByteArrayOutputStream errorStreamCaptor = new ByteArrayOutputStream();


    //Static Integer that measures our tries, times a certain method was called within a Function of The Library class
    public static Integer Tries = 0;
    static class NotificationServiceStub implements NotificationService{
        // This function will work as follows:
        // editing tries value will determine how many times the function will fail
        // in case tries = 0 then function will succeed that means it will not throw NotificationException
        //The purpose is to CHECK whether a certain function has been called the appropriete number of times
        public NotificationServiceStub(){

        }
        @Override
        public void notifyUser(String userId, String message) throws NotificationException {
            if(Tries != 0)
            {
                Tries = Tries-1;
                throw new NotificationException("Failed to Notify User!");
            }
        }
    }

    //Mocks and Stubs for isolating the Library class
    @Mock
    private static DatabaseService databaseServiceMock;
    @Mock
    private static ReviewService reviewServiceMock;

    private static NotificationService notificationServiceStub;

    // Class to be tested
    private Library library;

    @BeforeAll
    static void setupAll() {
        System.out.println("Initializing resources for all the tests");
        // This method will be executed before all test methods (e.g., Database Connection)
        // initialize mocks
        notificationServiceStub = new NotificationServiceStub();
    }

    @BeforeEach
    void setup() {
        System.out.println("Initializing resources before each test method");
        //MockitoAnnotations.openMocks(this);
        /*
        opeMocks has not worked for us, so we manually create our mock objects for use before each Test.
         */
        databaseServiceMock = mock(DatabaseService.class);
        reviewServiceMock = mock(ReviewService.class);
        library = new Library(databaseServiceMock, reviewServiceMock);
        //Reset Tries to 0
        Tries = 0;
        // This method will be executed before each test method (e.g., reset variables, like calculator)
        // Redirect the standard output to the outputStreamCaptor
        System.setOut(new PrintStream(outputStreamCaptor));
        System.setErr(new PrintStream(errorStreamCaptor));
    }





    //-------------------------------- Testing starts Here ---------------------------------





    // tests are implemented to cover all lines of code of the class Library.java
    //region tests for addBook function 1
    // test 1.1 : test function given book value is NULL, throw Illegal argument Exception.
    @Test
    public void givenNull_WhenAddBook_ThenThrowIllegalArgumentException() {
        //Assert that addbook() has been called with a NULL parameter properly and Exception thrown
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(null));
        //Check that proper exception has been thrown
        assertEquals("Invalid book.", exception.getMessage());
    }

    // test 1.2 : test function given book ISBN value is not valid
    //region tests for isISBNValid function 2 because it is a private method we will test it through addBook function
    // test 2.1 : test function given ISBN value is null
    @Test
    public void givenBookWithNullISBN_WhenIsAddBook_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = null;


        // Create a mock object for the Book class
        Book mockBook = mock(Book.class);
// Determine result returned when functions are called on Mock
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");
        when(mockBook.isBorrowed()).thenReturn(false);
        //Assert that addbook() has been called properly and Exception thrown
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(mockBook));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 2.2 : test function given not valid ISBN i.e: length is less than 13, more than 13, length 13 and not all numbers, length 13 and all digits and not valid
    @ParameterizedTest
    @ValueSource(strings = {"00","123456789123456789123456789","a2345b789c234","1234567891234"})
    public void givenBookWithISBNShorterThan13_WhenIsAddBook_ThenThrowIllegalArgumentException(String str) {
        //Setup Objects
        String isbn = str;

        // Create a mock object for the Book class
        Book mockBook = mock(Book.class);
// Determine result returned when functions are called on Mock
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");
        when(mockBook.isBorrowed()).thenReturn(false);
        //Assert that addbook() has been called properly and Exception thrown
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(mockBook));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }

    // test 2.3 : test function given Valid ISBN length is 13 (with - or without) and all digits and null Title
    @ParameterizedTest
    @ValueSource(strings = {"1234567891231","--1-2-3-4-5-6-7-8-9-1-2-3-1--"})
    public void givenBookWithValidISBNAndNullTitle_WhenIsAddBook_ThenThrowIllegalArgumentException(String str) {
        //Setup Objects
        String isbn = str;

        // Create a mock object for the Book class
        Book mockBook = mock(Book.class);
// Determine result returned when functions are called on Mock
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn(null);
        when(mockBook.getAuthor()).thenReturn("author");
        when(mockBook.isBorrowed()).thenReturn(false);
        //Assert that addbook() has been called properly and Exception thrown
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(mockBook));
        assertEquals("Invalid title.", exception.getMessage());
    }
    //endregion

    // test 1.3 : test function given book title value is null
    @Test
    public void givenBookWithNullTitle_WhenAddBook_ThenThrowIllegalArgumentException() {
        //Setup Objects

        // Create a mock object for the Book class
        Book mockBook = mock(Book.class);
// Determine result returned when functions are called on Mock
        when(mockBook.getISBN()).thenReturn("1234567891231");
        when(mockBook.getTitle()).thenReturn(null);
        when(mockBook.getAuthor()).thenReturn("author");
        when(mockBook.isBorrowed()).thenReturn(false);
        //Assert that addbook() has been called  properly and Exception thrown
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(mockBook));
        assertEquals("Invalid title.", exception.getMessage());
    }
    // test 1.4 : test function given book with not valid title value is ""
    @Test
    public void givenBookWithEmptyStringTitle_WhenAddBook_ThenThrowIllegalArgumentException() {
        //Setup Objects

        // Create a mock object for the Book class
        Book mockBook = mock(Book.class);
// Determine result returned when functions are called on Mock
        when(mockBook.getISBN()).thenReturn("1234567891231");
        when(mockBook.getTitle()).thenReturn("");
        when(mockBook.getAuthor()).thenReturn("author");
        when(mockBook.isBorrowed()).thenReturn(false);
        //Assert that addbook() has been called properly and Exception thrown
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(mockBook));
        assertEquals("Invalid title.", exception.getMessage());
    }

    // test 1.5 : test function given book author value is not valid
    //region tests for isAuthorValid function 3 because it is a private method we will test it through addBook function
    // test 3.1 : test function given author value is null
    @Test
    public void givenBookWithNullAuthor_WhenAddBook_ThenThrowIllegalArgumentException() {
        //Setup Objects

        // Create a mock object for the Book class
        Book mockBook = mock(Book.class);
// Determine result returned when functions are called on Mock
        when(mockBook.getISBN()).thenReturn("1234567891231");
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn(null);
        when(mockBook.isBorrowed()).thenReturn(false);
        //Assert that addbook() has been called  properly and Exception thrown
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(mockBook));
        assertEquals("Invalid author.", exception.getMessage());
    }
    // test 3.2 : test function given not valid author value  {empty string "" , end with non letter, start with non letter or both, with special characters
    @ParameterizedTest
    @ValueSource(strings = {"", "author1", "1author", "1author1", "aut#$or", "aut\\. .or", "1aut\\. .or1", "aut---hor", "auth@or", "auth $or", "auth-\\wr", "aut\'\'or", "auth\'$^r", "auth. @or"})
    public void givenBookWithNotValidAuthor_WhenAddBook_ThenThrowIllegalArgumentException(String str) {
        //Setup Objects

        Book mockBook = mock(Book.class);
// Determine result returned when functions are called on Mock
        when(mockBook.getISBN()).thenReturn("1234567891231");
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn(str);
        when(mockBook.isBorrowed()).thenReturn(false);
        //Assert that addbook() has been called  properly and Exception thrown
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(mockBook));
        assertEquals("Invalid author.", exception.getMessage());
    }
    //endregion
    // test 1.6 : test function given book with valid author and borrowed book
    @Test
    public void givenBookThatIsBorrowed_WhenAddBook_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = "1234567891231";

        Book mockBook = mock(Book.class);
// Determine result returned when functions are called on Mock
        doNothing().when(mockBook).borrow();
        when(mockBook.isBorrowed()).thenReturn(true);
        when(mockBook.getISBN()).thenReturn("1234567891231");
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");
        mockBook.borrow();
        verify(mockBook).borrow();
        assertTrue(mockBook.isBorrowed());
        //Assert that addbook() has been called properly and Exception thrown
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook((mockBook)));
        assertEquals("Book with invalid borrowed state.", exception.getMessage());
    }

    // test 1.7 : test function given book already in DataBase
    @Test
    public void givenBookThatIsInDatabase_WhenAddBook_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = "1234567891231";

        Book mockBook = mock(Book.class);
// Determine result returned when functions are called on Mock
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook((mockBook)));
        // Verify that the getBook method was called after all checks passed
        verify(databaseServiceMock).getBookByISBN(isbn);
        // assert result
        assertEquals("Book already exists.", exception.getMessage());
    }

    // test 1.8 : test function given book that passes all checks in the class and succeeded
    @Test
    public void givenValidBook_WhenAddBook_ThenAddBookToDataBase() {
        //Setup Objects
        String isbn = "1234567891231";

        // Create a mock object for the Book class
        Book book = mock(Book.class);
// Determine result returned when functions are called on Mock
        when(book.getISBN()).thenReturn("1234567891231");
        when(book.getTitle()).thenReturn("title");
        when(book.getAuthor()).thenReturn("author");
        when(book.isBorrowed()).thenReturn(false);

        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(null);
        library.addBook(book);
        // Verify that the getBook method was called after all checks passed
        verify(databaseServiceMock).getBookByISBN(isbn);
        // Verify that the addBook method was called after all checks passed
        verify(databaseServiceMock).addBook(isbn, book);
    }
    //endregion


    //region tests for registerUser function 4
    // test 4.1 : test function given user value is null
    @Test
    public void givenNullUser_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(null));
        // assert result
        assertEquals("Invalid user.", exception.getMessage());
    }
    // test 4.2 : test function given user id is Value is null
    @Test
    public void givenUserWithNullId_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        //Setup Objects

        User mockUser = mock(User.class);
// Determine result returned when functions are called on Mock
        when(mockUser.getName()).thenReturn("radwan");
        when(mockUser.getId()).thenReturn(null);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(mockUser));
        // assert result
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 4.3 : test function given not valid and not null user id: length less than 12, more than 12
    @ParameterizedTest
    @ValueSource(strings = {"00", "1234", "123456789123456789"})
    public void givenUserWithNotValidId_WhenRegisterUser_ThenThrowIllegalArgumentException(String str) {
        //Setup Objects

        User mockUser = mock(User.class);
// Determine result returned when functions are called on Mock
        when(mockUser.getName()).thenReturn("abed");
        when(mockUser.getId()).thenReturn(str);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(mockUser));
        // assert result
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 4.4 : test function given user id length is 12 and name is null
    @Test
    public void givenUserWithNullName_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        //Setup Objects

        User mockUser = mock(User.class);
// Determine result returned when functions are called on Mock
        when(mockUser.getName()).thenReturn(null);
        when(mockUser.getId()).thenReturn("123456789123");
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(mockUser));
        // assert result
        assertEquals("Invalid user name.", exception.getMessage());
    }
    // test 4.5 : test function given user id length is 12 and name is Empty string ""
    @Test
    public void givenUserWithEmptyName_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        //Setup Objects

       User mockUser = mock(User.class);
// Determine result returned when functions are called on Mock
        when(mockUser.getName()).thenReturn("");
        when(mockUser.getId()).thenReturn("123456789123");
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(mockUser));
        // assert result
        assertEquals("Invalid user name.", exception.getMessage());
    }
    // test 4.6 : test function given user id length is 12 and valid name and null notification service
    @Test
    public void givenUserWithNullNotificationService_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        //Setup Objects
        User mockUser = mock(User.class);
// Determine result returned when functions are called on Mock
        when(mockUser.getName()).thenReturn("abed");
        when(mockUser.getId()).thenReturn("123456789123");
        when(mockUser.getNotificationService()).thenReturn(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(mockUser));
        // assert result
        assertEquals("Invalid notification service.", exception.getMessage());
    }
    // test 4.7 : test function given user id length is 12 and valid name and valid notification service and user is in database
    @Test
    public void givenUserInDatabase_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String id = "123456789123";

        User mockUser = mock(User.class);
// Determine result returned when functions are called on Mock
        when(mockUser.getName()).thenReturn("abed");
        when(mockUser.getId()).thenReturn(id );
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(mockUser));
        //Verify function is called
        verify(databaseServiceMock).getUserById(id);
        // assert result
        assertEquals("User already exists.", exception.getMessage());
    }
    // test 4.8 : test function given user all checks passes and function succeeded
    @Test
    public void givenUser_WhenRegisterUser_ThenRegisterUserToDataBase() {
        //Setup Objects
        String id = "123456789123";
        User mockUser = mock(User.class);
        //Determine Returns of Mock Object
        when(mockUser.getName()).thenReturn("radwan");
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        when(databaseServiceMock.getUserById(id)).thenReturn(null);
        library.registerUser(mockUser);
        // Verify that the registerUser method was called after all checks passed
        verify(databaseServiceMock).getUserById(id);
        verify(databaseServiceMock).registerUser(id, mockUser);
    }

    //endregion

    //region tests for borrowBook function 5
    // test 5.1 : test function given book with not valid isbn value (null value)
    @Test
    public void givenNotValidIsbnAndUserId_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = null;
        String id = "123456789123";
        //Verify exception was thrown after proper function was called and assert result
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(isbn, id));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 5.2 : test function given valid isbn but not in Database and valid id, book null returned from databaseService
    @Test
    public void givenValidIsbnNotInDatabaseAndValidUserId_WhenBorrowBook_ThenThrowBookNotFoundException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(null);
        BookNotFoundException exception = assertThrows(BookNotFoundException.class, () -> library.borrowBook(isbn, id));
        //Verify Function was Called
        verify(databaseServiceMock).getBookByISBN(isbn);
        assertEquals("Book not found!", exception.getMessage());
    }
    // test 5.3 : test function given valid isbn and book in database and null id ,valid book returned from databaseService
    @Test
    public void givenValidIsbnInDatabaseAndNullUserId_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = "1234567891231";

        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");
        String id = null;
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(isbn, id));
        //Verify Function was Called
        verify(databaseServiceMock).getBookByISBN(isbn);
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 5.4 : test function given valid isbn and book in database and not valid id: length less than 12, more than 12,valid book returned from databaseService
    @ParameterizedTest
    @ValueSource(strings = {"1024", "12341234","1234567891231", "12345678912311234567891231"})
    public void givenValidIsbnInDatabaseAndUserIdLenLessThan12_WhenBorrowBook_ThenThrowIllegalArgumentException(String str) {
        //Setup Objects
        String isbn = "1234567891231";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");
        String id = str;
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(isbn, id));
        //Verify Function was Called
        verify(databaseServiceMock).getBookByISBN(isbn);
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 5.4 : test function given valid isbn and book in database and valid id but user not in database,valid book returned from databaseService
    @Test
    public void givenValidIsbnInDatabaseAndUserIdNotInDatabase_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = "1234567891231";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");
        String id = "123456789123";
        //Pre Determine return values for Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(null);
        UserNotRegisteredException exception = assertThrows(UserNotRegisteredException.class, () -> library.borrowBook(isbn, id));
        //Verify Function was Called
        verify(databaseServiceMock).getBookByISBN(isbn);
        verify(databaseServiceMock).getUserById(id);
        assertEquals("User not found!", exception.getMessage());
    }
    // test 5.5 : test function given valid isbn and book in database and valid id and user in database and book is borrowed
    @Test
    public void givenValidIsbnInDatabaseAndBookIsBorrowedAndValidUserIdInDatabase_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = "1234567891231";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");
        mockBook.borrow();
        when(mockBook.isBorrowed()).thenReturn(true);
        assertTrue(mockBook.isBorrowed());
        String id = "123456789123";
        User mockUser = mock(User.class);
        //Determine Returns of Mock Object
        when(mockUser.getName()).thenReturn("radwan");
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        //Pre Determine return values for Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);
        BookAlreadyBorrowedException exception = assertThrows(BookAlreadyBorrowedException.class, () -> library.borrowBook(isbn, id));
        //Verify Function was Called
        verify(databaseServiceMock).getBookByISBN(isbn);
        verify(databaseServiceMock).getUserById(id);
        assertEquals("Book is already borrowed!", exception.getMessage());
    }
    // test 5.6 : test function given valid isbn and book in database and valid id and user in database and book is not borrowed, successfully run
    @Test
    public void givenValidIsbnInDatabaseAndBookIsNotBorrowedAndValidUserIdInDatabase_WhenBorrowBook_ThenBorrowBookAndUpdateDatabase() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");

        User mockUser = mock(User.class);
        //Determine Returns of Mock Object
        when(mockUser.getName()).thenReturn("abed");
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        //Pre Determine return values for Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);
        library.borrowBook(isbn, id);
        when(mockBook.isBorrowed()).thenReturn(true);
        verify(mockBook).isBorrowed();
        //Verify Function was Called
        verify(databaseServiceMock).getBookByISBN(isbn);
        verify(databaseServiceMock).getUserById(id);
        //Assert that our function has worked
        assertTrue(mockBook.isBorrowed());
        //Verify Function was Called
        verify(databaseServiceMock).borrowBook(isbn, id);
    }



    //endregion


    //region tests for returnBook function 6
    // test 6.1 : test function given not valid book isbn (null value)
    @Test
    public void givenNullIsbn_WhenReturnBook_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = null;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.returnBook(isbn));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 6.2 : test function given not valid book isbn:
    @ParameterizedTest
    @ValueSource(strings = {"00","123456789123456789123456789","a2345b789c234","1234567891234", "0000000000001"})
    public void givenNotValidIsbn_WhenReturnBook_ThenThrowIllegalArgumentException(String str) {
        //Setup Objects
        String isbn = str;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.returnBook(isbn));
        //Assert that our function has worked
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 6.3 : test function given valid book isbn but databaseService.getBookByISBN return null book (null value)
    @ParameterizedTest
    @ValueSource(strings = {"1234567891231", "0000000000000"})
    public void givenValidIsbnNotInDatabase_WhenReturnBook_ThenThrowBookNotFoundException(String str) {
        //Setup Objects
        String isbn = str;
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(null);
        BookNotFoundException exception = assertThrows(BookNotFoundException.class, () -> library.returnBook(isbn));
        //Verify function was called
        verify(databaseServiceMock).getBookByISBN(isbn);
        //Assert that our function has worked
        assertEquals("Book not found!", exception.getMessage());
    }
    // test 6.4 : test function given valid book isbn in database and borrowed = false
    @Test
    public void givenValidIsbnInDatabaseAndBookNotBorrowed_WhenReturnBook_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = "1234567891231";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        BookNotBorrowedException exception = assertThrows(BookNotBorrowedException.class, () -> library.returnBook(isbn));
        //Verify function was called
        verify(databaseServiceMock).getBookByISBN(isbn);
        //Assert that our function has worked
        assertEquals("Book wasn't borrowed!", exception.getMessage());
    }
    // test 6.5 : test function given valid book isbn in database and borrowed = true
    @Test
    public void givenValidIsbnInDatabaseAndBookBorrowed_WhenReturnBook_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = "1234567891231";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");

        //Act out test
        mockBook.borrow();
        when(mockBook.isBorrowed()).thenReturn(true);
        assertTrue(mockBook.isBorrowed());
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        library.returnBook(isbn);
        when(mockBook.isBorrowed()).thenReturn(false);
        //Assert that our function has worked (Book  is not borrowed after return).
        assertFalse(mockBook.isBorrowed());
        // Verify that the getBook method was called
        verify(databaseServiceMock).getBookByISBN(isbn);
        // Verify that the returnBook method was called after all checks passed
        verify(databaseServiceMock).returnBook(isbn);
    }
    //endregion

    //region tests for notifyUserWithBookReviews function 7
    // test 7.1 : test function given not valid book isbn (null value)
    @Test
    public void givenNullIsbnAndId_WhenNotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = null;
        String id = "123456789123";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        //Assert that our function has worked (Invalid ISBN for book because it was null).
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 7.2 : test function given not valid book isbn
    //@ param: String str
    @ParameterizedTest
    @ValueSource(strings = {"00","123456789123456789123456789","a2345b789c234","1234567891234"})
    public void givenNotValidIsbnAndId_WhenNotifyUserWithBookReviews_ThenThrowIllegalArgumentException(String str) {
        //Setup Objects
        String isbn = str;
        String id = "123456789123";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        //Assert that our function has worked (Invalid ISBN for book because it was null).
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 7.3 : test function given valid book isbn and null id
    @Test
    public void givenIsbnAndNullId_WhenNotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = null;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        //Assert that our function has worked (Invalid ISBN for book because ID was null).
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 7.4 : test function given valid book isbn and id : shorter than 12, longer than 12
    @ParameterizedTest
    @ValueSource(strings = {"00","00123","0012321354654545465445"})
    public void givenIsbnAndIdShorterThan12_WhenNotifyUserWithBookReviews_ThenThrowIllegalArgumentException(String str) {
        //Setup Objects
        String isbn = "1234567891231";
        String id = str;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 7.5 : test function given valid book isbn and valid id and book not in database
    @Test
    public void givenIsbnAndIdAndBookNotInDatabase_WhenNotifyUserWithBookReviews_ThenThrowBookNotFoundException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(null);
        BookNotFoundException exception = assertThrows(BookNotFoundException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        verify(databaseServiceMock).getBookByISBN(isbn);
        assertEquals("Book not found!", exception.getMessage());
    }
    // test 7.6 : test function given valid book isbn and valid id and book in database, user not in database
    @Test
    public void givenIsbnAndIdAndUserNotInDatabase_WhenNotifyUserWithBookReviews_ThenThrowUserNotRegisteredException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");

        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(null);
        UserNotRegisteredException exception = assertThrows(UserNotRegisteredException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        verify(databaseServiceMock).getBookByISBN(isbn);
        verify(databaseServiceMock).getUserById(id);
        assertEquals("User not found!", exception.getMessage());
    }
    // test 7.7 : test function given valid book isbn and valid id and book in database, user in database, reviewService.getReviewsForBook throws ReviewException
    @Test
    public void givenIsbnAndIdAndBookAndNoValidReviews_WhenNotifyUserWithBookReviews_ThenThrowReviewServiceUnavailableException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");

        User mockUser = mock(User.class);
        //Determine Returns of Mock Object
        when(mockUser.getName()).thenReturn("abed");
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        //Pre determine returned values of Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);
        when(reviewServiceMock.getReviewsForBook(isbn)).thenThrow(new ReviewException("Review Not Valid."));
        ReviewServiceUnavailableException exception = assertThrows(ReviewServiceUnavailableException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        //Verify correct functions were called
        verify(databaseServiceMock).getBookByISBN(isbn);
        verify(databaseServiceMock).getUserById(id);
        verify(reviewServiceMock).getReviewsForBook(isbn);
        //Assert returned exception is valid one
        assertEquals("Review service unavailable!", exception.getMessage());
    }
    // test 7.8 : test function given valid book isbn and valid id and book in database, user in database, reviewService.getReviewsForBook returns null
    @Test
    public void givenIsbnAndIdAndBookNotInDatabaseAndNullReviews_WhenNotifyUserWithBookReviews_ThenThrowNoReviewsFoundException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");

        User mockUser = mock(User.class);
        //Determine Returns of Mock Object
        when(mockUser.getName()).thenReturn("radwan");
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        //Pre determine returned values of Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);
        when(reviewServiceMock.getReviewsForBook(isbn)).thenReturn(null);
        NoReviewsFoundException exception = assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        //Verify correct functions were called
        verify(databaseServiceMock).getBookByISBN(isbn);
        verify(databaseServiceMock).getUserById(id);
        verify(reviewServiceMock).getReviewsForBook(isbn);
        //Assert returned exception is valid one
        assertEquals("No reviews found!", exception.getMessage());
    }

    // test 7.9 : test function given valid book isbn and valid id and book in database, user in database, reviewService.getReviewsForBook returns Empty String List
    @Test
    public void givenIsbnAndIdAndBookNotInDatabaseAndEmptyListReviews_WhenNotifyUserWithBookReviews_ThenThrowNoReviewsFoundException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");

        User mockUser = mock(User.class);
        //Determine Returns of Mock Object
        when(mockUser.getName()).thenReturn("abed");
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        List<String> empty = new ArrayList<>();
        //Pre determine returned values of Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);
        when(reviewServiceMock.getReviewsForBook(isbn)).thenReturn(empty);
        NoReviewsFoundException exception = assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        //Verify correct functions were called
        verify(databaseServiceMock).getBookByISBN(isbn);
        verify(databaseServiceMock).getUserById(id);
        verify(reviewServiceMock).getReviewsForBook(isbn);
        //Assert returned exception is valid one
        assertEquals("No reviews found!", exception.getMessage());
    }

    // test 7.10 : test function given valid book isbn and valid id and book in database, user in database, user.sendNotification fails more than 5 times
    // @param: Integer number
    @ParameterizedTest
    @ValueSource(ints = {5, 6,7,8, 100})
    public void givenIsbnAndIdAndBookNotInDatabaseAndSendNotificationFailsMoreThan5Times_WhenNotifyUserWithBookReviews_ThenThrowNotificationException(Integer number) {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");

        User mockUser = mock(User.class);
        //Determine Returns of Mock Object
        when(mockUser.getName()).thenReturn("abed");
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        //Pre determine returned values of Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);
        List<String> reviews = new ArrayList<>();
        reviews.add("Review 1");
        when(reviewServiceMock.getReviewsForBook(isbn)).thenReturn(reviews);
        //Show that when we try at least 5 times to send a notification but fail, capture by error stream
        doAnswer(invocation -> {
            String message = invocation.getArgument(0);
            notificationServiceStub.notifyUser(id, message);
            return null; // assuming sendNotification is void
        }).when(mockUser).sendNotification(anyString());
        Tries = number; // notificationServiceStub.notifyUser will fail 100 times, more than 5 times
        NotificationException exception = assertThrows(NotificationException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        //Verify correct functions were called
        verify(databaseServiceMock).getBookByISBN(isbn);
        verify(databaseServiceMock).getUserById(id);
        verify(reviewServiceMock).getReviewsForBook(isbn);
        //Assert returned exception is valid one
        assertEquals("Notification failed!", exception.getMessage());
    }
    // test 7.11 : test function given valid book isbn and valid id and book in database, user in database, user.sendNotification fails less than 5 times
    // @param: Integer number
    @ParameterizedTest
    @ValueSource(ints = {0,1,2,3,4})
    public void givenIsbnAndIdAndBookNotInDatabaseAndSendNotificationFailsLessThan5Times_WhenNotifyUserWithBookReviews_ThenSucceed(Integer number) {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");

        User mockUser = mock(User.class);
        //Determine Returns of Mock Object
        when(mockUser.getName()).thenReturn("radwan");
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        //Show that when we try at least 5 times to send a notification but fail, capture by error stream
        doAnswer(invocation -> {
            String message = invocation.getArgument(0);
            notificationServiceStub.notifyUser(id, message);
            return null; // assuming sendNotification is void
        }).when(mockUser).sendNotification(anyString());
        //Pre determine returned values of Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);
        //Pre determine returned values of Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);


        List<String> reviews = new ArrayList<>();
        reviews.add("Review 1");
        when(reviewServiceMock.getReviewsForBook(isbn)).thenReturn(reviews);
        Tries = number; // notificationServiceStub.notifyUser will fail 5 times
        library.notifyUserWithBookReviews(isbn,id);
      //  doThrow(NotificationException.class).when(mockUser).sendNotification(anyString());
        //Verify correct functions were called
        verify(databaseServiceMock).getBookByISBN(isbn);
        verify(databaseServiceMock).getUserById(id);
        verify(reviewServiceMock).getReviewsForBook(isbn);

        //Assert returned error
        String errStrs = "";
        for(int i=0; i<number; i++){
            errStrs += "Notification failed! Retrying attempt " + (i+1) + "/5\r\n";
        }
        if(number > 0)
        {
            //FIX
            assertEquals(errStrs, errorStreamCaptor.toString());
        }
    }
    //endregion

    //region tests for getBookByISBN function 8
    // test 8.1 : test function given not valid book isbn (null value)
    @Test
    public void givenNullIsbnAndId_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = null;
        String id = "123456789123";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(isbn,id));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 8.2 : test function given not valid book isbn
    @ParameterizedTest
    @ValueSource(strings = {"00","123456789123456789123456789","a2345b789c234","1234567891234"})
    public void givenNotValidIsbnAndId_WhenGetBookByISBN_ThenThrowIllegalArgumentException(String str) {
        //Setup Objects
        String isbn = str;
        String id = "123456789123";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(isbn,id));
        //Assert returned exception is valid one
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 8.3 : test function given valid book isbn and not valid userId (null value)
    @Test
    public void givenValidIsbnAndNullId_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = null;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(isbn,id));
        //Assert returned exception is valid one
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 8.4 : test function given valid book isbn and not valid userId (not valid value shorter than 12 and longer than 12)
    // @param: String str
    @ParameterizedTest
    @ValueSource(strings = {"", "123", "123456", "12345678912", "1234567891223", "123456789121231231413","12345678912a"})
    public void givenValidIsbnAndNotValidId_WhenGetBookByISBN_ThenThrowIllegalArgumentException(String str) {
        //Setup Objects
        String isbn = "1234567891231";
        String id = str;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(isbn,id));
        //Assert returned exception is valid one
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 8.5 : test function given valid book isbn and valid userId and database service return null book
    @Test
    public void givenValidIsbnAndValidIdAndDatabaseReturnNull_WhenGetBookByISBN_ThenThrowBookNotFoundException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(null);
        BookNotFoundException exception = assertThrows(BookNotFoundException.class, () -> library.getBookByISBN(isbn,id));
        //Verify  getBookByISBN() was called with proper parameter
        verify(databaseServiceMock).getBookByISBN(isbn);
        //Assert returned exception is valid one
        assertEquals("Book not found!", exception.getMessage());
    }
    // test 8.6 : test function given valid book isbn and valid userId and database service return borrowed book
    @Test
    public void givenValidIsbnAndValidIdAndDatabaseReturnBorrowedBook_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");

        User mockUser = mock(User.class);
        //Determine Returns of Mock Object
        when(mockUser.getName()).thenReturn("radwan");
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        //Pre determine returned values of Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        mockBook.borrow();
        verify( mockBook).borrow();
        when(mockBook.isBorrowed()).thenReturn(true);
        assertTrue(mockBook.isBorrowed());
        //Pre determine returned values of Mocks
        BookAlreadyBorrowedException exception = assertThrows(BookAlreadyBorrowedException.class, () -> library.getBookByISBN(isbn,id));
        //Verify getBookByISBN(isbn) was called
        verify(databaseServiceMock).getBookByISBN(isbn);
        //Assert returned exception is valid one
        assertEquals("Book was already borrowed!", exception.getMessage());
    }

    // test 8.7 : test function given valid book isbn and valid userId and database service return not borrowed book and notify user fails
    @Test
    public void givenValidIsbnAndValidIdAndDatabaseReturnValidBookAndNoNotifyUser_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        Book returnedBook = library.getBookByISBN(isbn,id);
        verify(databaseServiceMock, times(2)).getBookByISBN(isbn);
        //Assert returned error is valid one
        assertEquals("Notification failed!\r\n", outputStreamCaptor.toString());
        //Assert function returned the correct book
        assertEquals(mockBook, returnedBook);
    }
    // test 8.8 : test function given valid book isbn and valid userId and database service return not borrowed book and notify user succeeded
    @Test
    public void givenValidIsbnAndValidIdAndDatabaseReturnValidBookAndNotifyUser_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        //Setup Objects
        String isbn = "1234567891231";
        String id = "123456789123";
        Book mockBook = mock(Book.class);
        //Determine Returns of Mock Object
        when(mockBook.getISBN()).thenReturn(isbn);
        when(mockBook.getTitle()).thenReturn("title");
        when(mockBook.getAuthor()).thenReturn("author");

        User mockUser = mock(User.class);
        //Determine Returns of Mock Object
        when(mockUser.getName()).thenReturn("abed");
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getNotificationService()).thenReturn(notificationServiceStub);
        //Pre determine returned values of Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);
        //Pre determine returned values of Mocks
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(mockBook);
        when(databaseServiceMock.getUserById(id)).thenReturn(mockUser);
        Tries= 0;

        List<String> reviews = new ArrayList<>();
        reviews.add("Review 1");
        when(reviewServiceMock.getReviewsForBook(isbn)).thenReturn(reviews);
        Book returnedBook = library.getBookByISBN(isbn,id);
        //Verify correct functions were called by Mocks
        verify(databaseServiceMock, times(2)).getBookByISBN(isbn);
        verify(databaseServiceMock).getUserById(id);
        verify(reviewServiceMock).getReviewsForBook(isbn);
        //Assert function returned the correct book
        assertEquals(mockBook, returnedBook);
    }
    //endregion


    //After Each Function Test, We tear down the resources of Library and reset our tries method, also make sure our Mocks point to null
    @AfterEach
    void tearDown() {
        System.out.println("Cleaning up resources after each test method");
        library = null;
        Tries = 0;
        //Reset System Stream paths for Outputs and Errors
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
        // This method will be executed after each test method (e.g., nullify variables, like calculator)
        databaseServiceMock = null;
        reviewServiceMock = null;
    }

    //After we finish all tests, we set our notificationServiceStub to null
    @AfterAll
    static void tearDownAll() {
        System.out.println("Cleaning up resources after all the tests");
        // This method will be executed after all test methods (e.g., close database connection)
        notificationServiceStub = null;
    }
}

