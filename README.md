# Spring Boot и Feature Flags
Есть достаточно много способов реализовать feature флаги, но самым распространенным способом является активация той или
иной имплементации некоторого интерфейса в зависимости от значения feature флага.

## ```application.yml``` и ```@ConditionalOnProperty```
Для feature флагов в Spring Boot приложениях очень удобно использовать application.yml. В этом файле чаще всего
определяется вся конфигурация приложения, и в то же время это может быть отличным местом и для наших feature флагов:
```yaml
feature-flags:
  is-new-books-service-enabled: true
  is-new-papers-service-enabled: false
```
Следующим шагом нам необходимо включать ту или иную имплементацию нашей фичи в зависимости от значения флага. Для этого
у нас есть аннотация аннотация ```@ConditionalOnProperty```:
```java
@Configuration
public class BooksServiceConfig {
    @Bean
    @ConditionalOnProperty(
            name = "feature-flags.is-new-books-service-enabled",
            havingValue = "false",
            matchIfMissing = true
    )
    public BooksService booksDefaultService() {
        return new BooksDefaultService();
    }
    @Bean
    @ConditionalOnProperty(
            name = "feature-flags.is-new-books-service-enabled",
            havingValue = "true"
    )
    public BooksService booksNewService() {
        return new BooksNewService();
    }
}
```
Если значение флага ```feature-flags.is-new-books-service-enabled``` - false, или этот флаг не задан в конфигурации, то
в контексте приложения будет создан объект класса ```BooksDefaultService```, а если значение true, то - ```BooksNewService```
соответственно. 
Вот как выглядят эти классы и интерфейс:
```java
public interface BooksService {
    String getBooksResult();
}
public final class BooksDefaultService implements BooksService {
    public String getBooksResult() {
        return "Default books implementation";
    }
}
public final class BooksNewService implements BooksService {
    public String getBooksResult() {
        return "New books implementation";
    }
}
```
И теперь, например в контроллере, мы можем использовать объект класса ```BooksService``` с актуальной имплементацией
(полиморфизм в действии):
```java
@RequestMapping("/books")
@RestController
@RequiredArgsConstructor
public final class BooksController {
    private final BooksService booksService;
}
```
Иногда необходимо выключить сам контроллер. Это делается тем же путем, с помощью аннотации ```@ConditionalOnProperty```:
```java
@RequestMapping("/books")
@RestController
@ConditionalOnProperty(
        name = "feature-flags.is-new-books-service-enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public final class BooksController {
}
```
Так вот если значение флага ```feature-flags.is-new-books-service-enabled``` - false, или этого флага нету в
application.yml, то мы получим ответ со статусом 404 на запрос к /books.

## ```@ConfigurationProperties```
Не только удобно но и очень важно инкапсулировать значения feature флагов в объект, который будет зарегистрирован в
контейнере Spring приложения. Для этого необходимо подключить зависимость ```spring-boot-configuration-processor```:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
</dependency>
```
Ну и дальше собственно наш тип с полями-флагами:
```java
@Getter
@Setter
@ConfigurationProperties(prefix = "feature-flags")
public class FeatureFlags {
	private Boolean isNewBooksServiceEnabled = false;
	private Boolean isNewPapersServiceEnabled = false;
}
```
Для того чтобы Spring собирал нам объект класса FeatureFlags со значениями из ```application.yml``` нужно сделать
следующее:
```java
@Configuration
@EnableConfigurationProperties(FeatureFlags.class)
public class FeatureFlagsConfig {
}
```
И теперь мы можем использовать объект класса ```FeatureFlags``` как bean-объект с помощью Spring DI:
```java
@Service
@RequiredArgsConstructor
public class SomeService {
    private FeatureFlags featureFlags;
}
```

## Обновление флагов в runtime
### Configuration Server
Чаще всего feature флаги нужно менять и подтягивать во время выполнения приложения. Один из вариантов - использование
Configuration Server'а. Configuration Server - это отдельное Spring Boot приложение, которое предоставляет HTTP API с
помощью которого основное приложение будет запрашивать свою конфигурацию. Вот пример простого Configuration Server'а:
```java
@EnableConfigServer
@SpringBootApplication
public class FeatureFlagsConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeatureFlagsConfigApplication.class, args);
    }
}
```
Так же нужно добавить зависимость:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```
Гибким решением будет использование базы данных для хранения значений feature флагов. Рассмотрим пример использования
```MySQL```. Создадим таблицу ```properties``` со следующей структурой:
```sql
CREATE TABLE `properties` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `application` VARCHAR(255),
  `profile` VARCHAR(255),
  `label` VARCHAR(255),
  `key` VARCHAR(255),
  `value` VARCHAR(255),
  `updatedOn` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;
```
Для работы с базой данных не забываем добавлять зависимости:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```
Добавим наши флаги в таблицу следующим образом:
```sql
INSERT INTO `properties` (`application`, `profile`, `label`, `key`, `value`) VALUES
('local', 'default', 'latest', 'feature-flags.is-new-books-service-enabled', 'true'),
('local', 'default', 'latest', 'feature-flags.is-new-papers-service-enabled', 'false');
```
Вот так выглядит ```application.yml``` Configuration Server'а:
```yaml
server.port: 8081
spring:
  application.name: config
  jmx.default-domain: config
  profiles.active: jdbc
  datasource:
    url: jdbc:mysql://localhost:3306/config
    password: password
    username: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  cloud.config.server.jdbc:
    order: 1
    sql: SELECT `key`, `value` FROM `properties` where `application`=? and `profile`=? and `label`=?
```
Посредством HTTP наше основное приложение будет получать feature флаги с Configuration Server'а, достаточно будет для
него добавить зависимость:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```
И ```bootstrap``` конфигурацию:
```yaml
spring:
    cloud.config:
        uri: http://localhost:8081
        profile: default
        label: latest
    application.name: local
```
Теперь у нас есть возможность хранить наши флаги в базе данных и подтягивать их в контекст основного приложения при старте.
### Spring Actuator и @RefreshScope
Но как быть когда приложение уже запущено, а нам необходимо включить ту или иную фичу? Здесь нам поможет Spring Actuator.
Добавим зависимость для него:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
По умолчанию доступ к refresh функциональности Actuator'а осуществляется посредством JMX, но есть возможность использовать
и через HTTP:
```yaml
management.endpoints.web.exposure.include: refresh
```
Для того чтобы значения флагов в объекте обновлялись необходимо добавить аннотацию ```@RefreshScope```:
```java
@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "feature-flags")
public class FeatureFlags {
	private Boolean isNewBooksServiceEnabled = false;
	private Boolean isNewPapersServiceEnabled = false;
}
```
Теперь мы можем подтягивать новые значения флагов с помощью POST /actuator/refresh.

Графически описанную архитектуру можно отобразить следующим образом:

![architecture](https://raw.githubusercontent.com/Onix-Systems/spring-feature-flags/master/spring-feature-flags.jpg)

Но каким образом будет меняться имплементация? Ведь bean объекты с нужной имплементацией создаются при запуске
приложения с помощью, как мы помним, аннотации ```@ConditionalOnProperty```, и после refresh они не изменятся.
Эта проблема решается с помощью ```@PostConstruct```, все той же аннотации ```@RefreshScope```(только использовать мы
ее будем уже для сервиса) и Proxy паттерна:
```java
public interface PapersService {
    String getPapersResult();
}
public final class PapersDefaultService implements PapersService {
    public String getPapersResult() {
        return "Default papers implementation";
    }
}
public final class PapersNewService implements PapersService {
    public String getPapersResult() {
        return "New papers implementation";
    }
}
@Service
@RefreshScope
@RequiredArgsConstructor
public class PapersServiceImpl implements PapersService {
    private final FeatureFlags featureFlags;
    private PapersService papersService;
    @PostConstruct
    private void init() {
        if (this.featureFlags.getIsNewPapersServiceEnabled()) {
            this.papersService = new PapersNewService();
        } else {
            this.papersService = new PapersDefaultService();
        }
    }
    public String getPapersResult() {
        return this.papersService.getPapersResult();
    }
}
```

## Frontend
Частенько новый функционал не ограничивается только изменениями на бекенде. Значения feature флагов также нужно отдавать
и на фронтенд. Для этого достаточно реализовать дополнительный эндпоинт GET /feature-flags:
```java
@RequestMapping("/feature-flags")
@RestController
@RequiredArgsConstructor
public final class FeatureFlagsController {
    private final FeatureFlags featureFlags;
    @GetMapping
    public FeatureFlags featureFlags() {
        return this.featureFlags;
    }
}
```

## Итоги
У Spring фреймворка, как оказалось, есть простые и гибкие инструменты для реализации feature флагов. Не нужно
использовать никаких дополнительных библиотек, не нужно тратить время на свою реализацию, достаточно разобраться с
тем что есть. Стандартные возможности фреймворка и готовые решения других Spring проектов(Spring Actuator,
Spring Cloud Config Server) неплохо покрывают все требования. Исходный код находится
[здесь](https://github.com/Onix-Systems/spring-feature-flags).
