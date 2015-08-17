Muezzin API
=================================

Welcome to Muezzin API web service!

This application provides Islamic prayer times for more than 200 countries, their cities and many of their districts. Data is read from Republic of Turkey, Presidency of Religious Affairs' [website](http://www.diyanet.gov.tr) and provided in a simple JSON structure.

The application will be running at https://muezzin.herokuapp.com.

API Reference
--------------
All endpoints use ```GET```requests at the moment. When there is an error, an error Json will be the response. It will have following format.

```
{
  "errors": [
    {
      "name": "Name of the error, like 'database', 'requestFailed' etc.",
      "value": "A relevant value; for example, if error is 'requestFailed', this might be status code (It might be null.)",
      "details": "A detailed message explaining the error (It might be null.)"
    },
    ... More here ...
  ]
}
```

****

###List of Countries
#####Path: [```/prayetimes/countries```](https://muezzin.herokuapp.com/prayertimes/countries)
It returns a list of all countries. The list will be sorted alphabetically by ```name``` field.

**Example Response**
```
{
  "countries": [
    ... More here ...
    {
      "id": 2,
      "name": "Turkey [Türkiye]",
      "trName": "Türkiye"
    },
    ... More here ...
  ]
}
```

**Details**

* **id** is a number. It is id of the country that Diyanet uses.
* **name** is the name of country as both English and native spelling.
* **trName** is the name of the country in Turkish.

****

###List of Cities of a Country
#####Path: [```/prayetimes/cities/<countryId>```](https://muezzin.herokuapp.com/prayertimes/cities/2)
It returns a list of all cities of given ```countryId```. Example is for Turkey.

**Example Response**
```
{
  "countryId": 2,
  "cities": [
    {
      "id": 500,
      "name": "Adana"
    },
    ... More here ...
  ]
}
```

**Details**

* **id** is a number. It is id of the city that Diyanet uses.
* **name** is the name of the city.

****

###List of Districts of a City
#####Path: [```/prayetimes/districts/<cityId>```](https://muezzin.herokuapp.com/prayertimes/districts/540)
It returns a list of all districts of given ```cityId```. Example is for İzmir. Please note that not every city has districts available. Diyanet only provides all of Turkey and some major cities.

**Example Response**
```
{
  "cityId": 540,
  "districts": [
    {
      "id": 9552,
      "name": "Aliağa"
    },
    ... More here ...
  ]
}
```

**Details**

* **id** is a number. It is id of the district that Diyanet uses.
* **name** is the name of the district.

****

###List of Prayer Times of a Country, City and District
#####Path: [```/prayetimes/<countryId>/<cityId>/<districtId>```](https://muezzin.herokuapp.com/2/540/9560)
It returns a list of prayer times for a month belonging to given ```countryId```, ```cityId``` and ```districtId```. Example is for Turkey, İzmir, İzmir. If you do not have district for your country and city, use endpoint without district id (See below). Please note that there are no times available for past dates and more than 1 month future dates. Diyanet only provides 1 month of prayer times starting from current time. Therefore, it is client's responsibility to keep requested prayer times and request more whenever needed.

**Example Response**
```
{
  "countryId": 2,
  "cityId": 540,
  "districtId": 9560,
  "times": [
    {
      "dayDate": 1439683200,
      "fajr": 1439696040,
      "shuruq": 1439701980,
      "dhuhr": 1439727720,
      "asr": 1439741340,
      "maghrib": 1439752740,
      "isha": 1439758140,
      "qibla": 1439728080
    },
    ... More here ...
  ]
}
```

**Details**

* **dayDate** is a timestamp value, representing the day. It has hours, minutes, seconds and milliseconds are set to 0. So you may format the date part as you wish.
* **fajr, shuruq, dhuhr, asr, maghrib, isha and qibla** are timestamp values, representing the named times respectively. They are actually shifted **dayDate** values by certain hours and minutes. Date part is the same as **dayDate**. So you may format the time part as you wish.

****

###List of Prayer Times of a Country and City with District
#####Path: [```/prayetimes/<countryId>/<cityId>```](https://muezzin.herokuapp.com/118/16382)
It returns a list of prayer times for a month belonging to given ```countryId``` and ```cityId``` with no ```districtId```. This endpoint should be used for cities that have no districts. Example is for Tunus, Al Qayrawan. Please note that there are no times available for past dates and more than 1 month future dates. Diyanet only provides 1 month of prayer times starting from current time. Therefore, it is client's responsibility to keep requested prayer times and request more whenever needed.

**Example Response**
```
{
  "countryId": 118,
  "cityId": 16382,
  "districtId": null,
  "times": [
    {
      "dayDate": 1439683200,
      "fajr": 1439696040,
      "shuruq": 1439701980,
      "dhuhr": 1439727720,
      "asr": 1439741340,
      "maghrib": 1439752740,
      "isha": 1439758140,
      "qibla": 1439728080
    },
    ... More here ...
  ]
}
```

**Details**

* **dayDate** is a timestamp value, representing the day. It has hours, minutes, seconds and milliseconds are set to 0. So you may format the date part as you wish.
* **fajr, shuruq, dhuhr, asr, maghrib, isha and qibla** are timestamp values, representing the named times respectively. They are actually shifted **dayDate** values by certain hours and minutes. Date part is the same as **dayDate**. So you may format the time part as you wish.

License
--------------
Muezzin API is licensed under the terms of the GNU General Public License version 3.

```
Muezzin API
Copyright (C) 2015  Mehmet Akif Tütüncü

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```