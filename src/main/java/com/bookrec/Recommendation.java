package com.bookrec;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
public class Recommendation {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  public String fromUser, toUser, isbn, note;
  public LocalDateTime createdAt = LocalDateTime.now();
}
