# Multiplatform HTTP Client

Мультиплатформенный HTTP клиент, который использует готовые HTTP клиенты, свойственные каждой платформе.

В `commonMain`, в пакете `ru.itmo.client` предоставлен интерфейс HTTP клиента, который необходимо реализовать.

Поддерживаются следующие платформы: JVM, JS (NodeJS, Browser).

Платформенные реализации размещены в соответствующих директориях `jvmMain`, `jsMain`.

Для отправки HTTP запросов используются следующие платформенные клиенты:

* JVM - [Java HTTP Client](https://openjdk.org/groups/net/httpclient/intro.html)
* Browser JS - [Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API)
* Node JS - [node-fetch](https://www.npmjs.com/package/node-fetch)
* Native (бонус) - [libcurl](https://curl.se/libcurl/)
