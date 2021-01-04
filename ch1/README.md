# 스프링 인 액션

## 스프링이란?

스프링 프레임워크는 Spring Application Context라는 컨테이너를 제공.

이 컨테이너에서 애플리케이션 컴포넌트(Bean)들을 생성하고 관리한다.

이때 Bean의 상호 연결은 DI(의존성 주입) 패턴을 기반으로 수행된다.

### DI (Dependency Injection)

객체를 직접 생성해서 사용하지 않고, 의존을 주입 받아 사용하는 방법.

- B 객체를 A 객체에서 직접 생성하는 경우

```java
public class A {

	private B b = new B(); // 객체를 직접 생성

	// main
	public static void main(String[] args) {
		A a = new A();
	}

}

```

- 외부에서 생성된 B객체를 생성자를 통해 의존 관계를 주입하는 경우 => DI

```java
public class A {

	private B b;

	public A(B b) {
		this.b = b;
	}

	// main
	public static void main(String[] args) {
		B b = new B(); //외부에서 생성 후
		A a = new A(b); // 의존 관계를 주입.
	}

}

```

이렇게 DI를 통해서 런타임 시에 의존 오브젝트를 사용할 수 있어, 모듈 간의 결합도가 낮아지고 유연성이 높아짐.

### Ioc(Inversion of Control)

제어의 역전으로, 객체의 의존관계에 대한 책임을 제 3자에게 위임하는 것을 IoC라고 함.

스프링에서는 Spring Application Context라는 컨테이너에서 컴포넌트들을 생성 및 관리하며, 컴포넌트(Bean)들은 Spring Application Context 컨테이너 내부에서 서로 연결되서
상호 작용을 이룸.

IoC를 통해서 객체간의 결합도를 줄일 수 있음

### (스프링) Bean 객체

Spring IoC 컨테이너가 관리하는 자바 객체

IoC 컨테이너에서 싱글톤 스코프로 객체를 관리

### 스프링 자동 구성

자동 구성은 Autowired와 Component Scanning이라는 스프링 기법을 기반으로 함.

Component Scanning을 사용해서 스프링은 자동으로 애플리케이션의 classpath에 지정된 컴포넌트를 찾은 후 스프링 애플리케이션 컨텍스트의 빈으로 생성할 수 있음. 또한 스프링은 Autowired를
사용해서 의존 관계가 있는 컴포넌트를 자동으로 다른 빈에 주입.

## 스프링 프로젝트 구조

- mvnw, mvnw.cmd
    - 메이븐 래퍼 스크립트, 메이븐이 로컬 컴포터에 설치되어 있지 않더라도, 이 스크립트를 사용해서 프로젝트를 빌드 할 수 있음.

- pom.xml
    - 메이븐 빌드 명세(우리 프로젝트를 빌드할 때 필요한 정보를 지정한 파일.)

```xml

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.6</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
```

- 이 태그에는 우리 프로젝트가 부모 POM으로 spring-boot-starter-parent를 갖는다는 것을 지정,
- 이 부모 POM은 스프링 프로젝트에서 흔히 사용되는 여러 라이브러리의 의존성 관리를 제공. => 이런 라이브러리들의 경우는 버전을 지정할 필요가 없음.

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

- 의존성은 <dependencies> 요소에 정의되며, 각 의존성이 <dependency> 요소로 지정됨.
- <artifactId>에 starter 단어를 포함 => spring-boot-starter 의존성을 나타냄.
- 이 의존성 항목들은 자체적으로 라이브러리 코드를 갖지 않고, 다른 라이브러리의 것을 사용함.

### starter 의존성의 장점

- 우리가 필요로 하는 모든 라이브러리의 의존성을 선언하지 않아도 되므로 빌드 파일이 훨씬 더 작아지고 관리하기 쉬워짐.
- 라이브러리 이름이 아닌 기능의 관점으로 의존성을 생각할 수 있음. => 웹 애플리케이션을 개발한대면 웹 애플리케이션을 작성할 수 있게 해주는 라이브러리들을 일일이 지정하는 대신에 여기처럼 웹 스타터 의존성만
  추가해주면 됨.
- 라이브러리들의 버전을 걱정하지 않아도 됨. 스프링 부트에 포함되는 라이브러리들의 버전은 호환이 보장되므로 사용하려는 스프링 부트 버전만 신경 쓰면 됨.

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 스프링 부트 플러그인의 기능

- 메이븐을 사용하는 애플리케이션을 실행할 수 있게 해줌.
- 의존성에 지정된 모든 라이브러리가 실행 가능 JAR 파일에 포함되어 있는지 그리고 런타임시에 classpath에서 찾을 수 있는지 확인함.
- 실행 가능 JAR 파일의 메인 클래스로 부트스트랩 클래스(SlaApplication)를 나타내는 매니페스트 파일을 JAR 파일에 생성.

### 애플리케이션의 bootstrap

```java
package tacos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // 스프링 부트 애플리케이션
public class SlaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlaApplication.class, args); // 애플리케이션을 실행한다.
	}

}

```

### @SpringBootApplication은 다음 세 개의 어노테이션이 결합한 것

- @SpringBootConfiguration: 현재 클래스를 구성 클래스로 지정.
- @EnableAutoConfiguration: 스프링 부트 자동-구성을 활성화 한다. (이 애노테이션이 우리가 필요로 하는 컴포넌트들을 자동으로 구성하도록 스프링 부트에 알려주는 역할)
- @ComponentScan: 컴포넌트 검색을 활성화 한다. => @Component, @Service, @Controller 등의 애노테이션과 함께 클래스를 선언할 수 있도록 해줌. => 스프링은 자동으로 그런
  클래스를 찾아서 스프링 애플리케이션 컨텍스트에 컴포넌트로 등록한다(빈으로 등록한다.)

```java
package tacos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SlaApplicationTests {

	@Test
	void contextLoads() {
	}

}

```

### 애플리케이션 테스트

-이 테스트 클래스는 스프링 애플리케이션 컨텍스트가 성공적으로 로드도리 수 있는지 확인하는 기본적인 검사를 수행.

- @SpringBootTest는 스프링 부트 기능으로 테스트를 시작하라는 것을 JUnit에 알려준다.