package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.NotificationService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestLibrary {
// Implement here
    // mocked external services / Dependencies

    // to check System.out.println() outputs from class Library
    private final PrintStream originalSystemOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    // to check System.err.println() outputs from class Library
    private final PrintStream originalSystemErr = System.err;
    private final ByteArrayOutputStream errorStreamCaptor = new ByteArrayOutputStream();

    public static Integer Tries = 0;
    static class NotificationServiceStub implements NotificationService{
        // this function will work as follows:
        // editing tries value will determine how many times the function will fail
        // in case tries = 0 then function will succeed that means it will not throw NotificationException
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
        databaseServiceMock = mock(DatabaseService.class);
        reviewServiceMock = mock(ReviewService.class);
        notificationServiceStub = new NotificationServiceStub();
    }

    @BeforeEach
    void setup() {
        System.out.println("Initializing resources before each test method");
        library = new Library(databaseServiceMock, reviewServiceMock);
        Tries = 0;

        // This method will be executed before each test method (e.g., reset variables, like calculator)
        // Redirect the standard output to the outputStreamCaptor
        System.setOut(new PrintStream(outputStreamCaptor));
        System.setErr(new PrintStream(errorStreamCaptor));
    }
    // tests are implemented to cover all lines of code of the class Library.java
    //region tests for addBook function 1
    // test 1.1 : test function given book value is null
    @Test
    public void givenNull_WhenAddBook_ThenThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(null));
        assertEquals("Invalid book.", exception.getMessage());
    }
    // test 1.2 : test function given book ISBN value is not valid
    //region tests for isISBNValid function 2 because it is a private method we will test it through addBook function
    // test 2.1 : test function given ISBN value is null
    @Test
    public void givenBookWithNullISBN_WhenIsAddBook_ThenThrowIllegalArgumentException() {
        String isbn = null;
        Book book = new Book(isbn, "title", "author");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 2.2 : test function given not valid ISBN i.e: length is less than 13, more than 13, length 13 and not all characters, length 13 and all digits and not valid
    @ParameterizedTest
    @ValueSource(strings = {"00","123456789123456789123456789","a2345b789c234","1234567891234"})
    public void givenBookWithISBNShorterThan13_WhenIsAddBook_ThenThrowIllegalArgumentException(String str) {
        String isbn = str;
        Book book = new Book(isbn, "title", "author");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }

    // test 2.3 : test function given Valid ISBN length is 13 (with - or without) and all digits and null Title
    @ParameterizedTest
    @ValueSource(strings = {"1234567891231","--1-2-3-4-5-6-7-8-9-1-2-3-1--"})
    public void givenBookWithValidISBNAndNullTitle_WhenIsAddBook_ThenThrowIllegalArgumentException(String str) {
        String isbn = str;
        Book book = new Book(isbn, null, "author");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid title.", exception.getMessage());
    }
    //endregion

    // test 1.3 : test function given book title value is null
    @Test
    public void givenBookWithNullTitle_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book("1234567891231", null, "author");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid title.", exception.getMessage());
    }
    // test 1.4 : test function given book with not valid title value is ""
    @Test
    public void givenBookWithEmptyStringTitle_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book("1234567891231", "", "author");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid title.", exception.getMessage());
    }

    // test 1.5 : test function given book author value is not valid
    //region tests for isAuthorValid function 3 because it is a private method we will test it through addBook function
    // test 3.1 : test function given author value is null
    @Test
    public void givenBookWithNullAuthor_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book book = new Book("1234567891231", "title", null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid author.", exception.getMessage());
    }
    // test 3.2 : test function given not valid author value  {empty string "" , end with non letter, start with non letter or both, with special characters
    @ParameterizedTest
    @ValueSource(strings = {"", "author1", "1author", "1author1", "aut#$or", "aut\\. .or", "1aut\\. .or1", "aut---hor"})
    public void givenBookWithNotValidAuthor_WhenAddBook_ThenThrowIllegalArgumentException(String str) {
        Book book = new Book("1234567891231", "title", str);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid author.", exception.getMessage());
    }
    //endregion
    // test 1.6 : test function given book with valid author and borrowed book
    @Test
    public void givenBookThatIsBorrowed_WhenAddBook_ThenThrowIllegalArgumentException() {
        String isbn = "1234567891231";
        Book book = new Book(isbn, "title", "author");
        book.borrow();
        assertTrue(book.isBorrowed());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.addBook((book)));
        assertEquals("Book with invalid borrowed state.", exception.getMessage());
    }

    // test 1.7 : test function given book already in DataBase
    @Test
    public void givenBookThatIsInDatabase_WhenAddBook_ThenThrowIllegalArgumentException() {
        String isbn = "1234567891231";
        Book book = new Book(isbn, "title", "author");
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        assertThrows(IllegalArgumentException.class, () -> library.addBook((book)));
    }

    // test 1.8 : test function given book that passes all checks in the class and succeeded
    @Test
    public void givenValidBook_WhenAddBook_ThenAddBookToDataBase() {
        String isbn = "1234567891231";
        Book book = new Book(isbn, "title", "author");
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(null);
        library.addBook(book);
        // Verify that the addBook method was called after all checks passed
        verify(databaseServiceMock).addBook(isbn, book);
    }
    //endregion


    //region tests for registerUser function 4
    // test 4.1 : test function given user value is null
    @Test
    public void givenNullUser_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(null));
        assertEquals("Invalid user.", exception.getMessage());
    }
    // test 4.2 : test function given user id is Value is null
    @Test
    public void givenUserWithNullId_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("radwan", null,notificationServiceStub);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 4.3 : test function given not valid and not null user id: length less than 12, more than 12
    @ParameterizedTest
    @ValueSource(strings = {"00", "1234", "123456789123456789"})
    public void givenUserWithNotValidId_WhenRegisterUser_ThenThrowIllegalArgumentException(String str) {
        User user = new User("radwan", str,notificationServiceStub);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 4.4 : test function given user id length is 12 and name is null
    @Test
    public void givenUserWithNullName_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User(null, "123456789123",notificationServiceStub);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
        assertEquals("Invalid user name.", exception.getMessage());
    }
    // test 4.5 : test function given user id length is 12 and name is Empty string ""
    @Test
    public void givenUserWithEmptyName_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("", "123456789123",notificationServiceStub);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
        assertEquals("Invalid user name.", exception.getMessage());
    }
    // test 4.6 : test function given user id length is 12 and valid name and null notification service
    @Test
    public void givenUserWithNullNotificationService_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("radwan", "123456789123",null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
        assertEquals("Invalid notification service.", exception.getMessage());
    }
    // test 4.7 : test function given user id length is 12 and valid name and valid notification service and user is in database
    @Test
    public void givenUserInDatabase_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        String id = "123456789123";
        User user = new User("radwan", id, notificationServiceStub);
        when(databaseServiceMock.getUserById(id)).thenReturn(user);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
        assertEquals("User already exists.", exception.getMessage());
    }
    // test 4.8 : test function given user all checks passes and function succeeded
    @Test
    public void givenUser_WhenRegisterUser_ThenRegisterUserToDataBase() {
        String id = "123456789123";
        User user = new User("radwan", id, notificationServiceStub);
        when(databaseServiceMock.getUserById(id)).thenReturn(null);
        library.registerUser(user);
        // Verify that the registerUser method was called after all checks passed
        verify(databaseServiceMock).registerUser(id, user);
    }

    //endregion

    //region tests for borrowBook function 5
    // test 5.1 : test function given book with not valid isbn value (null value)
    @Test
    public void givenNotValidIsbnAndUserId_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        String isbn = null;
        String id = "123456789123";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(isbn, id));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 5.2 : test function given valid isbn but not in Database and valid id, book null returned from databaseService
    @Test
    public void givenValidIsbnNotInDatabaseAndValidUserId_WhenBorrowBook_ThenThrowBookNotFoundException() {
        String isbn = "1234567891231";
        String id = "123456789123";
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(null);
        BookNotFoundException exception = assertThrows(BookNotFoundException.class, () -> library.borrowBook(isbn, id));
        assertEquals("Book not found!", exception.getMessage());
    }
    // test 5.3 : test function given valid isbn and book in database and null id ,valid book returned from databaseService
    @Test
    public void givenValidIsbnInDatabaseAndNullUserId_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        String isbn = "1234567891231";
        Book book = new Book(isbn, "title", "author");
        String id = null;
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(isbn, id));
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 5.4 : test function given valid isbn and book in database and not valid id: length less than 12, more than 12,valid book returned from databaseService
    @ParameterizedTest
    @ValueSource(strings = {"1024", "12341234","1234567891231", "12345678912311234567891231"})
    public void givenValidIsbnInDatabaseAndUserIdLenLessThan12_WhenBorrowBook_ThenThrowIllegalArgumentException(String str) {
        String isbn = "1234567891231";
        Book book = new Book(isbn, "title", "author");
        String id = str;
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(isbn, id));
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 5.4 : test function given valid isbn and book in database and valid id but user not in database,valid book returned from databaseService
    @Test
    public void givenValidIsbnInDatabaseAndUserIdNotInDatabase_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        String isbn = "1234567891231";
        Book book = new Book(isbn, "title", "author");
        String id = "123456789123";
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        when(databaseServiceMock.getUserById(id)).thenReturn(null);
        UserNotRegisteredException exception = assertThrows(UserNotRegisteredException.class, () -> library.borrowBook(isbn, id));
        assertEquals("User not found!", exception.getMessage());
    }
    // test 5.5 : test function given valid isbn and book in database and valid id and user in database and book is borrowed
    @Test
    public void givenValidIsbnInDatabaseAndBookIsBorrowedAndValidUserIdInDatabase_WhenBorrowBook_ThenThrowIllegalArgumentException() {
        String isbn = "1234567891231";
        Book book = new Book(isbn, "title", "author");
        book.borrow();
        assertTrue(book.isBorrowed());
        String id = "123456789123";
        User user = new User("radwan", id, notificationServiceStub);
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        when(databaseServiceMock.getUserById(id)).thenReturn(user);
        BookAlreadyBorrowedException exception = assertThrows(BookAlreadyBorrowedException.class, () -> library.borrowBook(isbn, id));
        assertEquals("Book is already borrowed!", exception.getMessage());
    }
    // test 5.6 : test function given valid isbn and book in database and valid id and user in database and book is not borrowed, successfully run
    @Test
    public void givenValidIsbnInDatabaseAndBookIsNotBorrowedAndValidUserIdInDatabase_WhenBorrowBook_ThenBorrowBookAndUpdateDatabase() {
        String isbn = "1234567891231";
        Book book = new Book(isbn, "title", "author");
        String id = "123456789123";
        User user = new User("radwan", id, notificationServiceStub);
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        when(databaseServiceMock.getUserById(id)).thenReturn(user);
        library.borrowBook(isbn, id);
        assertTrue(book.isBorrowed());
        verify(databaseServiceMock).borrowBook(isbn, id);
    }
    //endregion


    //region tests for returnBook function 6
    // test 6.1 : test function given not valid book isbn (null value)
    @Test
    public void givenNullIsbn_WhenReturnBook_ThenThrowIllegalArgumentException() {
        String isbn = null;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.returnBook(isbn));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 6.2 : test function given not valid book isbn:
    @ParameterizedTest
    @ValueSource(strings = {"00","123456789123456789123456789","a2345b789c234","1234567891234"})
    public void givenNotValidIsbn_WhenReturnBook_ThenThrowIllegalArgumentException(String str) {
        String isbn = str;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.returnBook(isbn));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 6.3 : test function given valid book isbn but databaseService.getBookByISBN return null book (null value)
    @Test
    public void givenValidIsbnNotInDatabase_WhenReturnBook_ThenThrowBookNotFoundException() {
        String isbn = "1234567891231";
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(null);
        BookNotFoundException exception = assertThrows(BookNotFoundException.class, () -> library.returnBook(isbn));
        assertEquals("Book not found!", exception.getMessage());
    }
    // test 6.4 : test function given valid book isbn in database and borrowed = false
    @Test
    public void givenValidIsbnInDatabaseAndBookNotBorrowed_WhenReturnBook_ThenThrowIllegalArgumentException() {
        String isbn = "1234567891231";
        Book book = new Book(isbn, "title", "author");
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        BookNotBorrowedException exception = assertThrows(BookNotBorrowedException.class, () -> library.returnBook(isbn));
        assertEquals("Book wasn't borrowed!", exception.getMessage());
    }
    // test 6.5 : test function given valid book isbn in database and borrowed = true
    @Test
    public void givenValidIsbnInDatabaseAndBookBorrowed_WhenReturnBook_ThenThrowIllegalArgumentException() {
        String isbn = "1234567891231";
        Book book = new Book(isbn, "title", "author");
        book.borrow();
        assertTrue(book.isBorrowed());
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        library.returnBook(isbn);
        assertFalse(book.isBorrowed());
        // Verify that the returnBook method was called after all checks passed
        verify(databaseServiceMock).returnBook(isbn);
    }
    //endregion

    //region tests for notifyUserWithBookReviews function 7
    // test 7.1 : test function given not valid book isbn (null value)
    @Test
    public void givenNullIsbnAndId_WhenNotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        String isbn = null;
        String id = "123456789123";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 7.2 : test function given not valid book isbn
    @ParameterizedTest
    @ValueSource(strings = {"00","123456789123456789123456789","a2345b789c234","1234567891234"})
    public void givenNotValidIsbnAndId_WhenNotifyUserWithBookReviews_ThenThrowIllegalArgumentException(String str) {
        String isbn = str;
        String id = "123456789123";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 7.3 : test function given valid book isbn and null id
    @Test
    public void givenIsbnAndNullId_WhenNotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        String isbn = "1234567891231";
        String id = null;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 7.4 : test function given valid book isbn and id : shorter than 12, longer than 12
    @ParameterizedTest
    @ValueSource(strings = {"00","00123","0012321354654545465445"})
    public void givenIsbnAndIdShorterThan12_WhenNotifyUserWithBookReviews_ThenThrowIllegalArgumentException(String str) {
        String isbn = "1234567891231";
        String id = str;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 7.5 : test function given valid book isbn and valid id and book not in database
    @Test
    public void givenIsbnAndIdAndBookNotInDatabase_WhenNotifyUserWithBookReviews_ThenThrowBookNotFoundException() {
        String isbn = "1234567891231";
        String id = "123456789123";
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(null);
        BookNotFoundException exception = assertThrows(BookNotFoundException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("Book not found!", exception.getMessage());
    }
    // test 7.6 : test function given valid book isbn and valid id and book in database, user not in database
    @Test
    public void givenIsbnAndIdAndUserNotInDatabase_WhenNotifyUserWithBookReviews_ThenThrowUserNotRegisteredException() {
        String isbn = "1234567891231";
        String id = "123456789123";
        Book book = new Book(isbn, "title", "author");
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        when(databaseServiceMock.getUserById(id)).thenReturn(null);
        UserNotRegisteredException exception = assertThrows(UserNotRegisteredException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("User not found!", exception.getMessage());
    }
    // test 7.7 : test function given valid book isbn and valid id and book in database, user in database, reviewService.getReviewsForBook throws ReviewException
    @Test
    public void givenIsbnAndIdAndBookAndNoValidReviews_WhenNotifyUserWithBookReviews_ThenThrowReviewServiceUnavailableException() {
        String isbn = "1234567891231";
        String id = "123456789123";
        Book book = new Book(isbn, "title", "author");
        User user = new User("radwan", id, notificationServiceStub);
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        when(databaseServiceMock.getUserById(id)).thenReturn(user);
        when(reviewServiceMock.getReviewsForBook(isbn)).thenThrow(new ReviewException("Review Not Valid."));
        ReviewServiceUnavailableException exception = assertThrows(ReviewServiceUnavailableException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("Review service unavailable!", exception.getMessage());
    }
    // test 7.8 : test function given valid book isbn and valid id and book in database, user in database, reviewService.getReviewsForBook returns null
    @Test
    public void givenIsbnAndIdAndBookNotInDatabaseAndNullReviews_WhenNotifyUserWithBookReviews_ThenThrowNoReviewsFoundException() {
        String isbn = "1234567891231";
        String id = "123456789123";
        Book book = new Book(isbn, "title", "author");
        User user = new User("radwan", id, notificationServiceStub);
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        when(databaseServiceMock.getUserById(id)).thenReturn(user);
        when(reviewServiceMock.getReviewsForBook(isbn)).thenReturn(null);
        NoReviewsFoundException exception = assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("No reviews found!", exception.getMessage());
    }

    // test 7.9 : test function given valid book isbn and valid id and book in database, user in database, reviewService.getReviewsForBook returns Empty String List
    @Test
    public void givenIsbnAndIdAndBookNotInDatabaseAndEmptyListReviews_WhenNotifyUserWithBookReviews_ThenThrowNoReviewsFoundException() {
        String isbn = "1234567891231";
        String id = "123456789123";
        Book book = new Book(isbn, "title", "author");
        User user = new User("radwan", id, notificationServiceStub);
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        when(databaseServiceMock.getUserById(id)).thenReturn(user);
        List<String> empty = new ArrayList<>();
        when(reviewServiceMock.getReviewsForBook(isbn)).thenReturn(empty);
        NoReviewsFoundException exception = assertThrows(NoReviewsFoundException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("No reviews found!", exception.getMessage());
    }

    // test 7.10 : test function given valid book isbn and valid id and book in database, user in database, user.sendNotification fails more than 5 times
    @ParameterizedTest
    @ValueSource(ints = {5, 6,7,8, 100})
    public void givenIsbnAndIdAndBookNotInDatabaseAndSendNotificationFailsMoreThan5Times_WhenNotifyUserWithBookReviews_ThenThrowNotificationException(Integer number) {
        String isbn = "1234567891231";
        String id = "123456789123";
        Book book = new Book(isbn, "title", "author");
        User user = new User("radwan", id, notificationServiceStub);
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        when(databaseServiceMock.getUserById(id)).thenReturn(user);
        List<String> reviews = new ArrayList<>();
        reviews.add("Review 1");
        when(reviewServiceMock.getReviewsForBook(isbn)).thenReturn(reviews);
        Tries = number; // notificationServiceStub.notifyUser will fail 100 time, more than 5 times
        NotificationException exception = assertThrows(NotificationException.class, () -> library.notifyUserWithBookReviews(isbn,id));
        assertEquals("Notification failed!", exception.getMessage());
    }
    // test 7.11 : test function given valid book isbn and valid id and book in database, user in database, user.sendNotification fails less than 5 times
    @ParameterizedTest
    @ValueSource(ints = {0,1,2,3,4})
    public void givenIsbnAndIdAndBookNotInDatabaseAndSendNotificationFailsLessThan5Times_WhenNotifyUserWithBookReviews_ThenSucceed(Integer number) {
        String isbn = "1234567891231";
        String id = "123456789123";
        Book book = new Book(isbn, "title", "author");
        User user = new User("radwan", id, notificationServiceStub);
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        when(databaseServiceMock.getUserById(id)).thenReturn(user);
        List<String> reviews = new ArrayList<>();
        reviews.add("Review 1");
        when(reviewServiceMock.getReviewsForBook(isbn)).thenReturn(reviews);
        Tries = number; // notificationServiceStub.notifyUser will fail 5 time
        library.notifyUserWithBookReviews(isbn,id);
        String errStrs = "";
        for(int i=0; i<number; i++){
            errStrs += "Notification failed! Retrying attempt " + (i+1) + "/5\r\n";
        }
        if(number > 0)
        {
            assertEquals(errStrs, errorStreamCaptor.toString());
        }
    }
    //endregion

    //region tests for getBookByISBN function 8
    // test 8.1 : test function given not valid book isbn (null value)
    @Test
    public void givenNullIsbnAndId_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        String isbn = null;
        String id = "123456789123";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(isbn,id));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 8.2 : test function given not valid book isbn
    @ParameterizedTest
    @ValueSource(strings = {"00","123456789123456789123456789","a2345b789c234","1234567891234"})
    public void givenNotValidIsbnAndId_WhenGetBookByISBN_ThenThrowIllegalArgumentException(String str) {
        String isbn = str;
        String id = "123456789123";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(isbn,id));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }
    // test 8.3 : test function given valid book isbn and not valid userId (null value)
    @Test
    public void givenValidIsbnAndNullId_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        String isbn = "1234567891231";
        String id = null;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(isbn,id));
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 8.4 : test function given valid book isbn and not valid userId (not valid value shorter than 12 and longer than 12)
    @ParameterizedTest
    @ValueSource(strings = {"", "123", "123456", "12345678912", "1234567891223", "123456789121231231413"})
    public void givenValidIsbnAndNotValidId_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        String isbn = "1234567891231";
        String id = null;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> library.getBookByISBN(isbn,id));
        assertEquals("Invalid user Id.", exception.getMessage());
    }
    // test 8.5 : test function given valid book isbn and valid userId and database service return null book
    @Test
    public void givenValidIsbnAndValidIdAndDatabaseReturnNull_WhenGetBookByISBN_ThenThrowBookNotFoundException() {
        String isbn = "1234567891231";
        String id = "123456789123";
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(null);
        BookNotFoundException exception = assertThrows(BookNotFoundException.class, () -> library.getBookByISBN(isbn,id));
        assertEquals("Book not found!", exception.getMessage());
    }
    // test 8.6 : test function given valid book isbn and valid userId and database service return borrowed book
    @Test
    public void givenValidIsbnAndValidIdAndDatabaseReturnBorrowedBook_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        String isbn = "1234567891231";
        String id = "123456789123";
        Book book = new Book(isbn, "title", "author");
        book.borrow();
        assertTrue(book.isBorrowed());
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        BookAlreadyBorrowedException exception = assertThrows(BookAlreadyBorrowedException.class, () -> library.getBookByISBN(isbn,id));
        assertEquals("Book was already borrowed!", exception.getMessage());
    }

    // test 8.7 : test function given valid book isbn and valid userId and database service return not borrowed book and notify user fails
    @Test
    public void givenValidIsbnAndValidIdAndDatabaseReturnValidBookAndNoNotifyUser_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        String isbn = "1234567891231";
        String id = "123456789123";
        Book book = new Book(isbn, "title", "author");
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        Book returnedBook = library.getBookByISBN(isbn,id);
        assertEquals("Notification failed!\r\n", outputStreamCaptor.toString());
        assertEquals(book, returnedBook);
    }
    // test 8.8 : test function given valid book isbn and valid userId and database service return not borrowed book and notify user succeeded
    @Test
    public void givenValidIsbnAndValidIdAndDatabaseReturnValidBookAndNotifyUser_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        String isbn = "1234567891231";
        String id = "123456789123";
        Book book = new Book(isbn, "title", "author");
        User user = new User("radwan", id, notificationServiceStub);
        Tries= 0;
        when(databaseServiceMock.getBookByISBN(isbn)).thenReturn(book);
        when(databaseServiceMock.getUserById(id)).thenReturn(user);
        List<String> reviews = new ArrayList<>();
        reviews.add("Review 1");
        when(reviewServiceMock.getReviewsForBook(isbn)).thenReturn(reviews);
        Book returnedBook = library.getBookByISBN(isbn,id);
        assertEquals(book, returnedBook);
    }
    //endregion

    @AfterEach
    void tearDown() {
        System.out.println("Cleaning up resources after each test method");
        library = null;
        Tries = 0;
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
        // This method will be executed after each test method (e.g., nullify variables, like calculator)
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("Cleaning up resources after all the tests");
        // This method will be executed after all test methods (e.g., close database connection)
        databaseServiceMock = null;
        reviewServiceMock = null;
        notificationServiceStub = null;
    }
}
