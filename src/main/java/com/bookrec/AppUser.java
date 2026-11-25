package com.bookrec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
@Entity @Table(name="app_user")
public class AppUser {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(unique=true,nullable=false) public String username;
  @JsonIgnore public String password;
  public String email;
  public String role;
}
