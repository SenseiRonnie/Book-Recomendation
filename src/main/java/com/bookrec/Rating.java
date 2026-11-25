package com.bookrec;
import jakarta.persistence.*;
@Entity @Table(uniqueConstraints=@UniqueConstraint(columnNames={"username","isbn"}))
public class Rating {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  public String username, isbn;
  public int stars;
}
