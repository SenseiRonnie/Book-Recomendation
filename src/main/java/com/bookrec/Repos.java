package com.bookrec;
import java.util.*;
import org.springframework.data.jpa.repository.*;
interface UserRepo extends JpaRepository<AppUser,Long> { Optional<AppUser> findByUsername(String u); }
interface BookRepo extends JpaRepository<Book,String> {}
interface RatingRepo extends JpaRepository<Rating,Long> {
  Optional<Rating> findByUsernameAndIsbn(String u,String isbn);
  long countByIsbn(String isbn);
  void deleteByIsbn(String isbn);
  @Query("select avg(r.stars) from Rating r where r.isbn=?1") Double avg(String isbn);
  @Query("select r.isbn, avg(r.stars), count(r) from Rating r group by r.isbn") List<Object[]> summary();
}
interface FriendRepo extends JpaRepository<Friendship,Long> {
  @Query("select f from Friendship f where (f.requester=?1 and f.receiver=?2) or (f.requester=?2 and f.receiver=?1)")
  Optional<Friendship> between(String a,String b);
  List<Friendship> findByReceiverAndStatus(String r,String s);
  @Query("select f from Friendship f where f.status='ACCEPTED' and (f.requester=?1 or f.receiver=?1)")
  List<Friendship> accepted(String u);
}
interface RecRepo extends JpaRepository<Recommendation,Long> { List<Recommendation> findByToUserOrderByIdDesc(String u); }
