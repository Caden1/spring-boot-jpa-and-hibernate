package com.learn.springboot.learnjpaandhibernate.course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.learn.springboot.learnjpaandhibernate.course.jdbc.CourseJdbcRepository;
import com.learn.springboot.learnjpaandhibernate.course.jpa.CourseJpaRepository;
import com.learn.springboot.learnjpaandhibernate.course.springdatajpa.CourseSpringDataJpaRepository;

@Component
public class CourseCommandLineRunner implements CommandLineRunner {
	
//	@Autowired
//	private CourseJdbcRepository repository;
	
//	@Autowired
//	private CourseJpaRepository repository;
	
	@Autowired
	private CourseSpringDataJpaRepository repository;

	@Override
	public void run(String... args) throws Exception {
		repository.save(new Course(1, "Learn AWS Now - JPA", "in28minutes"));
		repository.save(new Course(2, "Learn Azure Now - JPA", "in28minutes"));
		repository.save(new Course(3, "Learn DevOps Now - JPA", "in28minutes"));
		
		repository.deleteById(1l);
		
		System.out.println(repository.existsById(1l));
		System.out.println(repository.existsById(2l));
		
		System.out.println(repository.findById(2l));
		System.out.println(repository.findById(3l));
		
		System.out.println(repository.findByAuthor("in28minutes"));
		System.out.println(repository.findByAuthor(""));
		
		System.out.println(repository.findByName("Learn Azure Now - JPA"));
	}
	
}
