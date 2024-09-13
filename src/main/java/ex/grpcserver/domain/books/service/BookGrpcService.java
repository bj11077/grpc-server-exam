package ex.grpcserver.domain.books.service;

import bookstore.BookServiceGrpc;
import bookstore.Bookstore;
import ex.grpcserver.domain.books.entity.Book;
import ex.grpcserver.global.interceptor.AccessLoggingInterceptor;
import ex.grpcserver.global.interceptor.BasicAuthInterceptor;
import ex.grpcserver.global.utils.TimestampConverter;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * proto를 토해 만들어진 grpc서비스를 확장해서 사용
 */

@GrpcService(interceptors = { AccessLoggingInterceptor.class, BasicAuthInterceptor.class })
public class BookGrpcService extends BookServiceGrpc.BookServiceImplBase {

    private final BookService bookService;

    @Autowired
    public BookGrpcService(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void addBook(Bookstore.AddBookRequest request, StreamObserver<Bookstore.Book> responseObserver) {
        Book newBook = new Book();
        newBook.setTitle(request.getTitle());
        newBook.setPublisher(request.getPublisher());
        newBook.setPublishedDate(TimestampConverter.fromProto(request.getPublishedDate()));

        Book savedBook = bookService.saveBook(newBook);
        responseObserver.onNext(Bookstore.Book.newBuilder()
                .setTitle(newBook.getTitle())
                .setPublishedDate(TimestampConverter.toProto(savedBook.getPublishedDate()))
                .setPublisher(savedBook.getPublisher())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getBookDetails(Bookstore.GetBookDetailsRequest request, StreamObserver<Bookstore.Book> responseObserver) {
        Bookstore.Book bookDetail = bookService.findById(request.getBookId())
                .map(book -> Bookstore.Book.newBuilder()
                        .setTitle(book.getTitle())
                        .setPublishedDate(TimestampConverter.toProto(book.getPublishedDate()))
                        .setPublisher(book.getPublisher())
                        .build())
                .orElseThrow();

        responseObserver.onNext(bookDetail);
        responseObserver.onCompleted();
    }

    @Override
    public void listBooks(Bookstore.ListBooksRequest request, StreamObserver<Bookstore.Book> responseObserver) {
        List<Book> books = bookService.findAll();

        for (Book book : books) {
            responseObserver.onNext(
                    Bookstore.Book.newBuilder()
                            .setTitle(book.getTitle())
                            .setPublishedDate(TimestampConverter.toProto(book.getPublishedDate()))
                            .setPublisher(book.getPublisher())
                            .build()
            );
        }

        responseObserver.onCompleted();
    }

    @Override
    public void searchBooksByAuthor(Bookstore.SearchBooksByAuthorRequest request, StreamObserver<Bookstore.Book> responseObserver) {
        List<Book> books = bookService.findByAuthorName(request.getAuthorName());

        for (Book book : books) {
            responseObserver.onNext(Bookstore.Book.newBuilder()
                    .setTitle(book.getTitle())
                    .setPublishedDate(TimestampConverter.toProto(book.getPublishedDate()))
                    .setPublisher(book.getPublisher())
                    .build());
        }

        responseObserver.onCompleted();
    }
}
