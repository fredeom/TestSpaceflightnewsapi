To run project:
```
./mvnw spring-boot:run
```

API to use:
```
http://localhost:8080/allArticles
http://localhost:8080/article/{id}
http://localhost:8080/articlesByNewsSite/{newsSite}
```

Customize run properties and blacklist words in:
```
src/main/resources/application.properties
```

Комментарии работодателя:
* Нет разбиения на классы и методы. Процедурный стиль написания кода.
* Неправильный подход при написании API, нарушены принципы REST.
* Не совсем правильно используется application.properties.
* Некорректно реализована синхронизация.
* Хотелось увидеть использование Stream API из Java 8. 
