package model;

import java.io.Serializable;

//implements serialisable  so java can write to the file using objectOutputStream

public class User implements Serializable{

	/**
	 * added default serializable version from warning
	 */
	private static final long serialVersionUID = 1L;
	// variables
	private String name;
	private String studentId;
	private String email;
	private String password;
	private String department;
	private String role;

	// constructor with fields
	public User(String name, String studentId, String email, String password, String department, String role) {
		super();
		this.name = name;
		this.studentId = studentId;
		this.email = email;
		this.password = password;
		this.department = department;
		this.role = role;
	}

	// getters and setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	// to string
	@Override
	public String toString() {
		return "User [name=" + name + " role=" + role + "]";
	}

}
