package com.bookrec;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity
public class Book {
  @Id @NotBlank @Pattern(regexp="[0-9Xx-]{10,17}") public String isbn;
  @NotBlank public String title;
  public String author, publisher;
  public Integer publishYear;
}
