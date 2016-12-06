# android-rrr-pagination
Example of RRR pagination

## RRR

RRR means below 3 libraries
- [RxAndroid](https://github.com/ReactiveX/RxAndroid)
- [Retrofit](https://github.com/square/retrofit)
- [Realm](https://github.com/realm/realm-java)

## Behavior

- Searching github repositories with "android+language:java"
- Caching to Realm for 1 day
- Paging per 100 on the bottom of list

## Architecture

- Based on Master/Detail view application (almost as-is)
- Inspired by [Clean Architecture](https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html)
