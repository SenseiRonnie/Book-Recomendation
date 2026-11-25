package com.bookrec;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api")
public class ApiController {
  record RegisterReq(@NotBlank String username, @Size(min=6) String password, @Email String email){}
  record RateReq(@Min(1) @Max(5) int stars){}
  record RecReq(@NotBlank String toUser, @NotBlank String isbn, String comment){}

  final UserRepo users; final BookRepo books; final RatingRepo ratings; final FriendRepo friends; final RecRepo recs; final PasswordEncoder enc;
  ApiController(UserRepo u,BookRepo b,RatingRepo r,FriendRepo f,RecRepo c,PasswordEncoder e){ users=u; books=b; ratings=r; friends=f; recs=c; enc=e; }
  static ResponseStatusException nf(){ return new ResponseStatusException(HttpStatus.NOT_FOUND); }
  static ResponseStatusException bad(String m){ return new ResponseStatusException(HttpStatus.BAD_REQUEST,m); }

  // ---- auth / profile
  @PostMapping("/auth/register") Map<String,String> register(@Valid @RequestBody RegisterReq r){
    if(users.findByUsername(r.username()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Username taken");
    AppUser u=new AppUser(); u.username=r.username(); u.password=enc.encode(r.password()); u.email=r.email(); u.role="USER"; users.save(u);
    return Map.of("status","registered");
  }
  @GetMapping("/me") AppUser me(Principal p){ return users.findByUsername(p.getName()).orElseThrow(ApiController::nf); }
  @PutMapping("/me") AppUser update(Principal p,@RequestBody Map<String,String> b){
    AppUser u=me(p); if(b.get("email")!=null) u.email=b.get("email"); return users.save(u);
  }

  // ---- books & ratings
  @GetMapping("/books") List<Book> allBooks(){ return books.findAll(); }
  @GetMapping("/books/{isbn}") Map<String,Object> book(@PathVariable String isbn){
    Book b=books.findById(isbn).orElseThrow(ApiController::nf); Double avg=ratings.avg(isbn);
    return Map.of("book",b,"avgRating",avg==null?0:avg,"ratingCount",ratings.countByIsbn(isbn));
  }
  @PostMapping("/books/{isbn}/rate") Rating rate(@PathVariable String isbn,@Valid @RequestBody RateReq r,Principal p){
    books.findById(isbn).orElseThrow(ApiController::nf);
    Rating x=ratings.findByUsernameAndIsbn(p.getName(),isbn).orElseGet(Rating::new);
    x.username=p.getName(); x.isbn=isbn; x.stars=r.stars(); return ratings.save(x);
  }
  @GetMapping("/ratings/summary") List<Map<String,Object>> summary(){
    List<Map<String,Object>> out=new ArrayList<>();
    for(Object[] row: ratings.summary()){
      String isbn=(String)row[0]; String title=books.findById(isbn).map(b->b.title).orElse(isbn);
      out.add(Map.of("isbn",isbn,"title",title,"avg",((Number)row[1]).doubleValue(),"count",((Number)row[2]).longValue()));
    }
    return out;
  }

  // ---- friends
  @PostMapping("/friends/request/{to}") Friendship request(@PathVariable String to,Principal p){
    if(to.equals(p.getName())||users.findByUsername(to).isEmpty()) throw bad("Invalid user");
    if(friends.between(p.getName(),to).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Already requested or friends");
    Friendship f=new Friendship(); f.requester=p.getName(); f.receiver=to; f.status="PENDING"; return friends.save(f);
  }
  @GetMapping("/friends/requests") List<Friendship> incoming(Principal p){ return friends.findByReceiverAndStatus(p.getName(),"PENDING"); }
  @PostMapping("/friends/accept/{id}") Friendship accept(@PathVariable Long id,Principal p){
    Friendship f=friends.findById(id).orElseThrow(ApiController::nf);
    if(!f.receiver.equals(p.getName())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    f.status="ACCEPTED"; return friends.save(f);
  }
  @GetMapping("/friends") List<String> friendList(Principal p){
    return friends.accepted(p.getName()).stream().map(f->f.requester.equals(p.getName())?f.receiver:f.requester).toList();
  }

  // ---- recommendations
  @PostMapping("/recommendations") Recommendation recommend(@Valid @RequestBody RecReq r,Principal p){
    boolean ok=friends.between(p.getName(),r.toUser()).filter(f->"ACCEPTED".equals(f.status)).isPresent();
    if(!ok) throw bad("You can only recommend to friends");
    books.findById(r.isbn()).orElseThrow(ApiController::nf);
    Recommendation c=new Recommendation(); c.fromUser=p.getName(); c.toUser=r.toUser(); c.isbn=r.isbn(); c.note=r.comment(); return recs.save(c);
  }
  @GetMapping("/recommendations") List<Map<String,Object>> inbox(Principal p){
    List<Map<String,Object>> out=new ArrayList<>();
    for(Recommendation c: recs.findByToUserOrderByIdDesc(p.getName())){
      Book b=books.findById(c.isbn).orElse(null);
      out.add(Map.of("from",c.fromUser,"isbn",c.isbn,"title",b==null?c.isbn:b.title,"author",b==null||b.author==null?"":b.author,
                     "comment",c.note==null?"":c.note,"at",c.createdAt.toString()));
    }
    return out;
  }

  // ---- admin
  @GetMapping("/admin/users") List<AppUser> allUsers(){ return users.findAll(); }
  @DeleteMapping("/admin/users/{id}") void delUser(@PathVariable Long id){
    AppUser u=users.findById(id).orElseThrow(ApiController::nf);
    if("ADMIN".equals(u.role)) throw bad("Cannot delete admin");
    users.delete(u);
  }
  @PostMapping("/admin/books") Book addBook(@Valid @RequestBody Book b){
    if(books.existsById(b.isbn)) throw new ResponseStatusException(HttpStatus.CONFLICT,"ISBN exists");
    return books.save(b);
  }
  @DeleteMapping("/admin/books/{isbn}") @Transactional void delBook(@PathVariable String isbn){ ratings.deleteByIsbn(isbn); books.deleteById(isbn); }
}
