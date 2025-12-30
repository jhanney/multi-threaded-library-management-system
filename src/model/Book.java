package model;

import java.io.Serializable;

public record Book(String bookId, String title, String author) implements Serializable {

}
