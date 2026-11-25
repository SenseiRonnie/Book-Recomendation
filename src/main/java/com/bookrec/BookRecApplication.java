package com.bookrec;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
@SpringBootApplication
public class BookRecApplication {
  public static void main(String[] a){ SpringApplication.run(BookRecApplication.class,a); }
  @Bean CommandLineRunner seed(UserRepo u, BookRepo b, PasswordEncoder e){
    return x -> {
      if(u.findByUsername("admin").isEmpty()){
        AppUser a=new AppUser(); a.username="admin"; a.password=e.encode("admin123"); a.role="ADMIN"; a.email="admin@example.com"; u.save(a);
      }
      if(b.count()==0){
        b.save(book("9780743273565","The Great Gatsby","F. Scott Fitzgerald","Scribner",2004));
        b.save(book("9780451524935","1984","George Orwell","Signet Classics",1950));
        b.save(book("9780061120084","To Kill a Mockingbird","Harper Lee","Harper Perennial",2002));
      }
    };
  }
  static Book book(String i,String t,String a,String p,int y){ Book b=new Book(); b.isbn=i; b.title=t; b.author=a; b.publisher=p; b.publishYear=y; return b; }
}
