Muezzin API
=================================

Welcome to Muezzin API web service!

This application provides Islamic prayer times for more than 200 countries, their cities and many of their districts. Data is read from Republic of Turkey, Presidency of Religious Affairs' [**website**](http://www.diyanet.gov.tr) and provided in a simple JSON structure.

The application will be running at **https://muezzin.herokuapp.com**.

Technical Details
--------------
Muezzin API application is developed using [**Play Framework**](https://www.playframework.com/) and [**Scala**](http://www.scala-lang.org/). The application utilizes [**Akka Actors**](http://akka.io/) actors for scheduling jobs, [**WS**](https://www.playframework.com/documentation/latest/ScalaWS) for making HTTP requests, [**Firebase Realtime Database**](https://firebase.google.com/docs/database/) for data persistance and [**Errors**](https://github.com/mehmetakiftutuncu/Errors) for error handling.

API Reference
-------------
###General
* All endpoints use `GET` method.
* When a request is successful, response will be `200 OK` with `application/json` as `Content-Type` and the requested data in body.
* When a request fails, response will be `503 SERVICE_UNAVAILABLE` with `application/json` as `Content-Type` and error data in body according to [**Errors**](https://github.com/mehmetakiftutuncu/Errors). 

#####Example Response With Errors
```json
{
  "errors": [
    ...,
    {
      "name": "requestFailed",
      "reason": "Diyanet returned content type.",
      "data": "321"
    },
    ...
  ]
}
```

* Each object in `errors` array will correspond to a single error. There may be 1 or more error objects.
* `name` will be provided for all types of errors whereas `reason` and `data` might not be available for all errors.

***

###Countries
####GET: [`/countries`](https://muezzin.herokuapp.com/countries)
It returns available countries.

#####Example Response
```json
{
  "countries": {
    ...,
    "2": {
      "name": "Turkey",
      "nameTurkish": "Türkiye",
      "nameNative": "Türkiye"
    },
    ...
  ]
}
```

* Every key in `countries` object is the id for the country in the value object.
* `name` is the name of country in English.
* `nameTurkish` is the name of the country in Turkish.
* `nameNative` is the name of the country in their native language.

***

###Cities
####GET: [`/countries/<countryId>/cities`](https://muezzin.herokuapp.com/countries/2/cities)
It returns available cities of given `countryId`.

#####Example Response
```json
{
  "cities": {
    ...,
    "540": {
      "name": "İzmir"
    },
    ...
  }
}
```

* Every key in `cities` object is the id for the city in the value object.
* `name` is the name of the city.

***

###Districts
####GET: [`/countries/<countryId>/cities/<cityId>/districts`](https://muezzin.herokuapp.com/countries/2/cities/540/districts)
It returns available districts of given `cityId` of `countryId`. Please note that not every city has districts available. Diyanet only provides districts of cities of Turkey and some other major countries.

#####Example Response
```json
{
  "districts": {
    ...,
    "9552": {
      "name": "Aliağa"
    },
    ...
  }
}
```

#####Example Response for a City With No Districts Available
```json
{
  "districts": {}
}
```

* Every key in `districts` object is the id for the district in the value object.
* `name` is the name of the district.

***

###Prayer Times
####GET: [`/prayerTimes/country/<countryId>/city/<cityId>/district/<districtId>`](https://muezzin.herokuapp.com/prayerTimes/country/2/city/540/district/9560)
####GET: [`/prayerTimes/country/<countryId>/city/<cityId>`](https://muezzin.herokuapp.com/prayerTimes/country/118/city/16382)
It returns prayer times for a month belonging to given `countryId`, `cityId` and `districtId`. If you do not have district for your country and city, use second endpoint without district id (See above). Please note that there are no times available for past dates and more than 1 month future dates. Diyanet only provides 1 month of prayer times starting from current time. Therefore, it is client's responsibility to keep requested prayer times and request more whenever needed.

#####Example Response
```json
{
  "prayerTimes": {
    ...,
    "2016-08-23": {
      "fajr": "04:56",
      "shuruq": "06:26",
      "dhuhr": "13:21",
      "asr": "17:02",
      "maghrib": "20:03",
      "isha": "21:26",
      "qibla": "12:00"
    },
    ...
  }
}
```

* Every key in ``prayerTimes`` object is the date for the prayer times in the value object. Dates are formatted as: `yyyy-MM-dd`
* `fajr`, `shuruq`, `dhuhr`, `asr`, `maghrib`, `isha` and `qibla` are times formatted as `HH:mm`, representing the named times respectively.

***

License
--------------
Muezzin API is licensed under the terms of the MIT License.

```
MIT License

Copyright (c) 2016 Mehmet Akif Tütüncü

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
