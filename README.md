This repository contains my practice and notes from section 6 of the Udemy course [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners), created by Ranga Karanam, the founder of in28minutes.

# Section 6: Getting Started with JPA and Hibernate with Spring and Spring Boot

Create project from Spring Initializr
* Add dependencies (starter projects):
    * Spring Web
    * Spring Data JDBC
    * Spring Data JPA
    * H2 Database
        * In-memory database

Launching up H2 Console and Creating Course Table in H2:
* When you launch a project that has H2 Database as a dependency, the console will log something like this:
    * com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection conn0: url=jdbc:h2:mem:50af44a5-95ae-4d65-ad27-754e8fa80ace user=SA
        * The “url” logged is where the database is running, in this case it’s:
            * jdbc:h2:mem:50af44a5-95ae-4d65-ad27-754e8fa80ace
* To configure the database connection:
    * Go to “application.properties” and add the following:
        * spring.h2.console.enabled=true
* Now you can go to “http://localhost:8080/h2-console” to access the h2-console through a web browser
    * When you open this, the url in the “JDBC URL” section is incorrect
    * To get the correct URL, look in the console logs and find the log that says something similar to:
        * H2 console available at '/h2-console'. Database available at 'jdbc:h2:mem:69d7fd21-6c66-43bb-987d-60853a4989f1'
    * Copy the “jdbc:h2:mem:69d7fd21-6c66-43bb-987d-60853a4989f1”, paste it into the “JDBC URL” section, and click “Connect”
    * This will connect you to the Database
* Since this database URL is dynamic, we should set up a property that allows it to use the new URL every time it’s needed. To do this:
    * Go back to “application.properties” and add:
        * spring.datasource.url=jdbc:h2:mem:testdb
    * Restart the server, refresh “http://localhost:8080/h2-console”, and paste the new url property as the URL and press “Connect”
￼

* Create tables in the H2 database:
    * In Eclipse, right click the project’s “src/main/resources” file -> new -> SQL File (might have to click “Other” and search for “SQL File”)
    * In the “File name” text box, type “schema.sql”, then click “Finish”
    * In the “schema.sql” file, add the following:
create table course
(
	id bigint not null,
	name varchar(255) not null,
	author varchar(255) not null,
	primary key (id)
);
    * Don’t forget the semi-colon after the closing parentheses!
    * In Java H2 database, use bigint rather than long
    * In databases, String = varchar, and the 255 is the number of characters allowed
    * Whenever you create a table, you should define a primary key for it. This allows each row in the table to have a unique identifier. In this case, the “id” is the primary key
    * When making use of Spring Data JPA Starter, it will automatically pickup the file called “schema.sql” and create the table in H2
        * When you restart the server and refresh the database connection, the table will be created

Getting Started with Spring JDBC:
* In the H2 Console (in the web browser) you can insert data into a table by using Structured Query Language (SQL) Statements:
insert into course (id, name, author)
values(1,'Learn AWS','in28minutes');
* I can then see the data by running:
select * from course;
* To delete the row:
delete from course where id=1;
* JDBC
    * Write a lot of SQL Queries
    * Write a lot of Java code
    * Example:
	public void deleteTodo(int id) {
		PreparedStatement st = null;
		try {
			st = db.conn.prepareStatement("delete from todo where id=?");
			st.setInt(1, id);
			st.execute();
		} catch (SQLException e) {
			logger.fatal("Query Failed : ", e);
		} finally {
			if (st != null) {
				try {st.close();}
				catch (SQLException e) {}
			}
		}
	}

* Spring JDBC
    * Write a lot of SQL Queries
    * BUT less Java code
    * Example:
	public void deleteTodo(int id) {
		JdbcTemplate.update("delete from todo where id=?", id);
	}

* Executing SQL Queries with Spring JDBC:
    * It’s good practice to save your SQL Queries in your project; create a .txt file called “notes.txt” and save them there
        * Example contents of “notes.txt” file:
insert into course (id, name, author)
values(1,'Learn AWS','in28minutes');

select * from course;

delete from course where id=1;


Inserting Hardcoded Data using Spring JDBC:
* @Repository annotation above class:
    * Tells Spring the class it’s talking to a database
* Use three double quotes to start and end the SQL Query String
* Example class using Spring JDBC:
@Repository
public class CourseJdbcRepository {
	@Autowired
	private JdbcTemplate springJdbcTemplate;
	private static String INSERT_QUERY = 
			"""
				insert into course (id, name, author)
				values(1,'Learn AWS','in28minutes');
			""";
	public void insert() {
		springJdbcTemplate.update(INSERT_QUERY);
	}
}

* To ensure the above class is executed when the application starts up:
    * Use Command Line Runner (Provided by Spring Boot)
    * Create a new class that implements CommandLineRunner; add the unimplemented method
        * CommandLineRunner is an interface used to indicate that a bean should run when it is contained within a SpringApplication
        * Use it when you have logic that needs to be run at the start of a Spring application (the “run” method is executed)
        * For this to work, this class:
            * Needs the @Component annotation
            * Needs an instance of the Repository (class) executing the SQL Statement
            * In the “run” method, use the instance of the repository to call its method that executes the SQL
            * Example:
@Component
public class CourseJdbcCommandLineRunner implements CommandLineRunner {
	@Autowired
	private CourseJdbcRepository repository;
	@Override
	public void run(String... args) throws Exception {
		repository.insert();
	}	
}

* Now run the application and check the H2 Console
* Do a “select * from course” and the data should be there

Inserting and Deleting Data using Spring JDBC:
* We want to create a class that holds the fields for what we want to insert into our database table:
public class Course {
	private long id;
	private String name;
	private String author;
	public Course() { }
	public Course(long id, String name, String author) {
		super();
		this.id = id;
		this.name = name;
		this.author = author;
	}
	public long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getAuthor() {
		return author;
	}
	@Override
	public String toString() {
		return "Course [id=" + id + ", name=" + name + ", author=" + author + "]";
	}	
}

* Then we change the Repository class’s SQL Query to use Question Marks (prepared statement, parameterized statement, or parameterized query), and the method to accept a Course parameter and the Course classes getters to insert the data:
@Repository
public class CourseJdbcRepository {
	@Autowired
	private JdbcTemplate springJdbcTemplate;
	private static String INSERT_QUERY = 
			"""
				insert into course (id, name, author)
				values(?, ?, ?);
			""";
	public void insert(Course course) {
		springJdbcTemplate.update(INSERT_QUERY, course.getId(), course.getName(), course.getAuthor());
	}
}

* Side Note:
    * Benefits of prepared statements:
        * Efficiency, because they can be used repeatedly without re-compiling
        * Security, by reducing or eliminating SQL injection attacks
* And we modify the class that implements the CommandLineRunner interface:
@Component
public class CourseJdbcCommandLineRunner implements CommandLineRunner {
	@Autowired
	private CourseJdbcRepository repository;
	@Override
	public void run(String... args) throws Exception {
		repository.insert(new Course(1, "Learn AWS Now", "in28minutes"));
		repository.insert(new Course(2, "Learn Azure Now", "in28minutes"));
		repository.insert(new Course(3, "Learn DevOps Now", "in28minutes"));
	}	
}

* Now add code to delete by id:
@Repository
public class CourseJdbcRepository {
	@Autowired
	private JdbcTemplate springJdbcTemplate;
	private static String INSERT_QUERY = 
			"""
				insert into course (id, name, author)
				values(?, ?, ?);
			""";
	private static String DELETE_QUERY = 
			"""
				delete from course
				where id=?;
			""";
	public void insert(Course course) {
		springJdbcTemplate.update(INSERT_QUERY, course.getId(), course.getName(), course.getAuthor());
	}
	public void deleteById(long id) {
		springJdbcTemplate.update(DELETE_QUERY, id);
	}
}

@Component
public class CourseJdbcCommandLineRunner implements CommandLineRunner {
	@Autowired
	private CourseJdbcRepository repository;
	@Override
	public void run(String... args) throws Exception {
		repository.insert(new Course(1, "Learn AWS Now", "in28minutes"));
		repository.insert(new Course(2, "Learn Azure Now", "in28minutes"));
		repository.insert(new Course(3, "Learn DevOps Now", "in28minutes"));	
		repository.deleteById(1);
	}	
}

Querying Data using Spring JDBC:
* When we want to do a select statement, it needs to be done differently than a insert or delete
* We can use:
private static String SELECT_QUERY = 
		"""
			select * from course
			where id=?;
		""";

* But now it returns a Course, and we need to get the result set back and map it to the Course bean using Row Mappers:
public Course findById(long id) {
	return springJdbcTemplate.queryForObject(SELECT_QUERY, new BeanPropertyRowMapper<>(Course.class), id);
}

* The above method uses the “queryForObject” method. This method’s second parameter takes a BeanPropertyRowMapper that allows you to specify the class to map to. The third parameter is the argument passed to the prepared statement
* For this to work, we also need to add setters to our Course class:
public class Course {
	private long id;
	private String name;
	private String author;
	public Course() { }
	public Course(long id, String name, String author) {
		super();
		this.id = id;
		this.name = name;
		this.author = author;
	}
	public long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getAuthor() {
		return author;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	@Override
	public String toString() {
		return "Course [id=" + id + ", name=" + name + ", author=" + author + "]";
	}
}

* I’m adding print statements to the “run” method to show the results:
@Component
public class CourseJdbcCommandLineRunner implements CommandLineRunner {
	@Autowired
	private CourseJdbcRepository repository;
	@Override
	public void run(String... args) throws Exception {
		repository.insert(new Course(1, "Learn AWS Now", "in28minutes"));
		repository.insert(new Course(2, "Learn Azure Now", "in28minutes"));
		repository.insert(new Course(3, "Learn DevOps Now", "in28minutes"));
		repository.deleteById(1);	
		System.out.println(repository.findById(2));
		System.out.println(repository.findById(3));
	}	
}

Getting Started with JPA and EntityManager:
* For JDBC, the queries you have to write in the Java code might get complex and hard to manage
* JPA solves this issue by providing the ability to map your bean (in this case the Course class) directly to the table in the database (in this case the Course table)
    * Basically mapping our Java class to our database table
* We do this by adding the “@Entity” annotation to the Java class
    * This one is from the “jakarta.persistence.Entity” import
    * If the Java class name differs from the table name you can map it in the “@Entity” annotation like this:
        * @Entity(name=“Table_Name”)
* We also make use of the “@Id” annotation to assign one of the Java fields as the primary key
* Use the “@Column()” annotation to map Java fields to their corresponding table columns
    * The “@Column()” annotation isn’t actually needed at all if the Java field names are the same as the database column names, but if they differ, you’ll need to map them like this:
@Column(name=“column_name")
private String name;
@Column(name="column_author”)
private String author;

* To interact with our class marked with @Entity, we create a new Repository (Java Class), make it Transactional with the “@Transactional” annotation, and use a “@PersistenceContext” annotation (more specific Autowiring) EntityManager in it as a field
* To insert into our database table we use the EntityManager’s “merge” method. Through the annotations, Spring handles the insertions
* To select using JPA, use EntityManager’s “find” method; it only needs the class and id (primary key)
* To delete using JPA, use EntityManager’s “remove” method; you first need to find it, then remove it
* Examples:
@Entity
public class Course {
	@Id
	private long id;
	private String name;
	private String author;
	public Course() { }
	public Course(long id, String name, String author) {
		super();
		this.id = id;
		this.name = name;
		this.author = author;
	}
	public long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getAuthor() {
		return author;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	@Override
	public String toString() {
		return "Course [id=" + id + ", name=" + name + ", author=" + author + "]";
	}	
}

@Repository
@Transactional
public class CourseJpaRepository {
	@PersistenceContext
	private EntityManager entityManager;
	public void insert(Course course) {
		entityManager.merge(course);
	}
	public Course findById(long id) {
		return entityManager.find(Course.class, id);
	}
	public void deleteById(long id) {
		Course course = entityManager.find(Course.class, id);
		entityManager.remove(course);
	}	
}

* Using my “CourseJpaRepository” class instead of my “CourseJdbcRepository” class:
@Component
public class CourseCommandLineRunner implements CommandLineRunner {
	@Autowired
	private CourseJpaRepository repository;
	@Override
	public void run(String... args) throws Exception {
		repository.insert(new Course(1, "Learn AWS Now - JPA", "in28minutes"));
		repository.insert(new Course(2, "Learn Azure Now - JPA", "in28minutes"));
		repository.insert(new Course(3, "Learn DevOps Now - JPA", "in28minutes"));
		repository.deleteById(1);	
		System.out.println(repository.findById(2));
		System.out.println(repository.findById(3));
	}
}

* If you want your logs to show the SQL Statements that are executed when using JPA, you can add the following to the “application.properties” file:
    * spring.jpa.show-sql=true
* Using JPA, we got rid of the need for manually writing SQL Statements

Exploring the Magic of JPA:
* Do NOT worry about queries
* Just map Entities to Tables
* Use annotations @Entity, @Transactional, @PersistenceContext, @Id, and maybe @Column

Getting Started with Spring Data JPA:
* Makes JPA even more simple!
* Takes care of everything!
* In order for this to work we need to create and interface
    * In out example we create an interface called “CourseSpringDataJpaRepository” because it’s for the “Course” class and database table
    * This interface extends the JpaRepository interface, which requires the Java Class its mapping and the type of the Id field (primary key). Example:
public interface CourseSpringDataJpaRepository extends JpaRepository<Course, Long> {

}

* Now, we need to change the “CourseCommandLineRunner” class to use this interface
    * To insert we use “save”
    * It provides “deleteById” and “findById” methods, but needs an ‘l’ after the id because it’s of type Long
    * We don’t even need to use the “@PersistenceContext” annotation anymore
    * A lot of useful methods are exposed to you, such as checking if an entity with a specific id exists using:
        * repository.existsById(Long id)
* New CourseCommandLineRunner:
@Component
public class CourseCommandLineRunner implements CommandLineRunner {
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
	}	
}

* The only things needed are the CourseCommandLineRunner class above, the CourseSpringDataJpaRepository repository that implements JpaRepository, and the Course (@Entity) class
    * THIS IS SO MUCH LESS CODE!

Exploring features of Spring Data JPA:
* Useful methods:
    * findAll()
        * Returns all data in table
    * count()
        * Returns number of Entities in table
* You can also add custom methods
    * In the interface we created that implements JpaRepository, we can add custom methods by following a naming convention
        * The naming convention is:
            * findBy[AttributeName]
                * Example to find by our author attribute:
                    * findByAuthor
    * Our new interface:
public interface CourseSpringDataJpaRepository extends JpaRepository<Course, Long> {
	List<Course> findByAuthor(String author);
}

* And after adding a method to find by name:
public interface CourseSpringDataJpaRepository extends JpaRepository<Course, Long> {
	List<Course> findByAuthor(String author);
	List<Course> findByName(String name);
}

* New CourseCommandLineRunner:
@Component
public class CourseCommandLineRunner implements CommandLineRunner {
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

Understanding difference between Hibernate and JPA:
* Dependencies:
    * JPA:
        * spring-boot-starter-data-jpa -> hibernate-core-jakarta -> jakarta.persistence-api
    * Hibernate:
        * spring-boot-starter-data-jpa -> hibernate-core-jakarta -> hibernate-commons-annotations
* All of the above dependencies are coming from “spring-boot-starter-data-jpa”, but in our code we’re only making use of the JPA annotations
* Hibernate vs JPA:
    * JPA defines the specification. It is an API.
        * It’s like an interface
        * How do you define entities? With @Entity
        * How do you define attributes? With @Column
        * Who manages the entities? The EntityManager
    * Then what is Hibernate?
        * Hibernate is one of the most popular implementations of JPA
        * You can choose to use Hibernate directly in your code, but if you do, you are locked into Hibernate
    * There are other JPA implementations as well (Toplink, for example)
* JPA is an API and Hibernate is an implementation of JPA
* Don’t use Hibernate directly in your code so you don’t get locked into Hibernate
