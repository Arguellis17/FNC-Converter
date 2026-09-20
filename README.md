# FNC Converter

Aplicativo para depuración y conversión de Gramáticas Libres de Contexto (GLC)
a Forma Normal de Chomsky (FNC), con visualización paso a paso.

Teoría de la Computación — Microproyecto #1.

## Descripción

El aplicativo permite:

1. Ingresar una Gramática Libre de Contexto G = (V, T, P, S).
2. Validar sus componentes antes de transformar.
3. Ejecutar la depuración y transformación paso a paso o en proceso completo.
4. Obtener una gramática equivalente en Forma Normal de Chomsky (FNC).

## Proceso de transformación (en orden)

1. Eliminación de producciones nulas (ε-producciones).
2. Eliminación de producciones unitarias (A → B).
3. Eliminación de variables inútiles (no generadoras).
4. Eliminación de variables inalcanzables (desde S).
5. Conversión a FNC: sustitución de terminales en producciones largas
   y reducción a producciones binarias (A → BC o A → a).

Cada etapa muestra la gramática antes y después, con las producciones
eliminadas y agregadas (historial completo).

## Requisitos

- JDK 25 o superior (probado con JDK 26).
- Apache Maven 3.9+ (o usar el plugin de Maven integrado de IntelliJ).
- IntelliJ IDEA (recomendado).
- Las dependencias de JavaFX se descargan automáticamente vía Maven
  (`org.openjfx`, versión 25).

## Estructura del proyecto

```text
src/main/java/com/fnc/
├── Main.java                 # Punto de entrada JavaFX
├── model/
│   ├── Grammar.java          # G = (V, T, P, S)
│   ├── Production.java       # Regla de producción
│   └── TransformationStep.java # Historial antes/después
├── engine/                   # Lógica de transformación
│   ├── GrammarValidator.java
│   ├── NullProductionEliminator.java
│   ├── UnitProductionEliminator.java
│   ├── UselessVariableEliminator.java
│   ├── UnreachableVariableEliminator.java
│   └── ChomskyNormalFormConverter.java
└── ui/                       # Interfaz gráfica JavaFX
    ├── MainFrame.java        # Ventana, menú y modos
    ├── GrammarInputPanel.java# Formulario G = (V, T, P, S)
    ├── TransformationPanel.java # Historial paso a paso
    └── ResultPanel.java      # Gramática final + validación FNC
```

## Ejecución

```bash
# Compilar
mvn compile

# Ejecutar la interfaz gráfica
mvn javafx:run
```

En IntelliJ también se puede crear una configuración `Application`
con `Main class: com.fnc.Main` (ver `.idea/runConfigurations/`).

## Formato de entrada

- Cada caracter es un símbolo: los espacios y comas son opcionales.
- Variables y terminales: Ej: `S, A, B` o `SAB`.
- Producciones: una por línea, con `->` o `→` y alternativas con `|`. Ej:

```text
S -> AB | B
A -> aA | a | ε
B -> bB | b
```

## Ramas de trabajo

- `master`: rama protegida (requiere Pull Request + 1 aprobación).
- `feature/engine-logic`: motor de transformación (mergeado vía PR #1).
- `feature/ui`: interfaz gráfica JavaFX.

Flujo: trabajar en ramas `feature/*`, abrir PR hacia `master`,
revisar/aprobar y mergear. No hacer push directo a `master`.

## Entregables del microproyecto

- [x] Código fuente funcional (motor + interfaz).
- [ ] Documento técnico (portada, planteamiento, objetivos, marco teórico,
      requerimientos, análisis/diseño, algoritmos, implementación,
      pruebas, resultados, conclusiones, referencias).
- [ ] Manual de usuario (instalación, ingreso de gramáticas, ejecución
      paso a paso y automática, interpretación de resultados, errores,
      ejemplo completo).

## Equipo

- Juan Arguello (Arguellis17) — motor de transformación (`engine`).
- Miguel Romero (miguelRomero1025) — interfaz gráfica (`ui`).
