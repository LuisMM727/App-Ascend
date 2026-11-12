# HOME Android — Iglesia Joven

Módulo Android independiente con Kotlin, Jetpack Compose, Room, MVVM y Repository Pattern.

## Abrir

Abra la carpeta `android-home` como proyecto en Android Studio, sincronice Gradle y ejecute `app`.

## Datos

Las entidades respetan los nombres del esquema SQL oficial. `MainActivity` usa el usuario `1`
como sesión de demostración. Antes de mostrar datos se debe poblar Room desde la API REST o
mediante un callback de base de datos.

Las fechas se almacenan como epoch milliseconds (`Long`). La contraseña solo se conserva en la
entidad para reflejar el esquema recibido; una aplicación real debe autenticar contra el backend
y guardar únicamente tokens cifrados con Android Keystore.

El esquema entregado no contiene rachas, monedas ni progreso independiente de misión. El progreso
de la misión se calcula a partir de sus actividades e inscripciones completadas.
