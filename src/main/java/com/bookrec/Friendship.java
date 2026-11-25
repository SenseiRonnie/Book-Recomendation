package com.bookrec;
import jakarta.persistence.*;
@Entity
public class Friendship {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  public String requester, receiver, status; // PENDING | ACCEPTED
}
