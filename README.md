# FNC Converter

Aplicativo para Depuración y Conversión de Gramáticas Libres de Contexto a Forma Normal de Chomsky.

## Descripción

Este proyecto desarrolla un aplicativo en Java con interfaz gráfica que permite:

1. Ingresar una Gramática Libre de Contexto (GLC)
2. Validar sus componentes
3. Realizar proceso de depuración y transformación paso a paso
4. Obtener una gramática equivalente en Forma Normal de Chomsky (FNC)

## Procesos de Transformación

- Eliminación de variables inútiles
- Eliminación de variables inalcanzables
- Eliminación de producciones nulas
- Eliminación de producciones unitarias
- Conversión a estructura FNC

## Requisitos

- JDK 26 o superior
- IntelliJ IDEA

## Ejecución

```bash
# Compilar
javac -d bin src/**/*.java

# Ejecutar
java -cp bin Main
```

## Autor

- Teoría de la Computación - Microproyecto #1
