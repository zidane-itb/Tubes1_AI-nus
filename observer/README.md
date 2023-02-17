# AI-nus
Bot AI-nus dibuat untuk memainkan permainan [Entelect Galaxio](https://github.com/EntelectChallenge/2021-Galaxio/)

Dalam pembuatannya, program ini memanfaatkan konsep algoritma *greedy* yang diajarkan melalui Kelas IF2211 Strategi Algoritma.

## Algoritma Greedy
Algoritma greedy yang digunakan dalam pembuatan program ini adalah aplikasi dasar solusi permasalahan `activity selection problem` dan `job scheduling problem`. Selain itu, program ini juga mengaplikasikan konsep `greedy by density` yang terbukti mangkus dalam mengatasi permasalahan yang disediakan dalam spesifikasi.

## Requirement Program 
Requirement dasar :
1. [Java](https://www.oracle.com/java/technologies/downloads/#java8) (minimal 11)
2. [InteliJ IDEA](https://www.jetbrains.com/idea/)
3. Net Core 3.1
   1. [Windows](https://dotnet.microsoft.com/download/dotnet/thank-you/sdk-3.1.407-windows-x64-installer)
   2. [Linux](https://docs.microsoft.com/en-us/dotnet/core/install/linux)
   3. [MacOS](https://dotnet.microsoft.com/download/dotnet/thank-you/sdk-3.1.407-macos-x64-installer)

Secara detail, *requirement* package program beserta versi yang diperlukan terdapat dalam file `pom.xml`.

Untuk memudahkan dalam proses instalasi/*resolve* dependensi, gunakan perintah maven
```bash
> mvn clean install
```
preintah `clean` ditambahkan untuk memudahkan dalam penataan direktori agar sesuai dengan file `pom.xml`.

>note : dalam file `pom.xml` yang kami buat, sepertinya program masih akan ter-compile menjadi jar bernama JavaBot.jar (bukan AI-nus.jar yang diminta oleh spesifikasi) sehingga dapat menghasilkan error ketika perintah `clean` ditambahkan


## How to Run
Berikut langkah-langkah untuk menjalankan program bot AI-nus

### Melakukan Kompilasi Program
untuk memudahkan dalam proses kompilasi, *compile* program dengan memanggil perintah maven, yaitu 
```bash
> mvn clean package
```

### Menjalakan Program
Untuk menjalankan program, jalankan file `AI-nus.jar` yang terdapat dalam folder `target/` menggunakan perintah java
```bash
> java -jar target/AI-nus.jar
```

Perhatikan juga bahwa setelah dijalankan, program ini akan berusaha untuk melakukan koneksi ke host lokal dengan alamat `https://localhost:5000/` yaitu alamat koneksi yang dibuka oleh server yang dijalankan dengan menjalankan runner, logger, dan engine server yang terdapat dalam folder starter-pack yang disediakan dalam spesifikasi.


>note : dalam file `pom.xml` yang kami buat, sepertinya program masih akan ter-compile menjadi jar bernama JavaBot.jar (bukan AI-nus.jar yang diminta oleh spesifikasi) sehingga dapat menghasilkan error ketika perintah `clean` ditambahkan

## Author(s)
1. [13521047 - Muhammad Equilibrie Fajria](https://github.com/MuhLibri)
2. [13521091 - Fakih Anugerah Pratama](https://github.com/fakihap)
3. [13521163 - Zidane Firzatullah](https://github.com/zidane-itb)
