## Требования
- Java: Версия 17.
- Maven: 3.6.3 или новее (`mvn -version`).
- Библиотеки:
    - Apache Commons CLI 1.5.0 (https://commons.apache.org/proper/commons-cli/)
    - SLF4J API 2.0.7 (https://www.slf4j.org/)
    - Logback Classic 1.4.7 (https://logback.qos.ch/)
    - JUnit Jupiter 5.9.2 (тесты) (https://junit.org/junit5/)

## Особенности
- Обработка аргументов через Commons CLI.
- Логи ошибок в `output/logs/error_details.log` (SLF4J/Logback).
- Директория `output` создаётся автоматически.
- Точка входа: `com.example.Application`.

Шаги запуска
Для запуска должны быть настроены версия Java и Maven(корректная настройка системных переменных(например path и JAVA_HOME))

mvn -version

java --version

1. Скопируйте код, например, в `F:\IJ\TestTask`.
2. Выполните(mvn/mvnd) `mvn clean install` в терминале.
3. перейдите в директорию например cd F:\IJ\TestTask  
   3.1 Запустите: `java -jar target/TestTask-1.0-SNAPSHOT.jar <аргументы>`.
    - Примеры:
        - `--sort=name --order=asc --stat`
        - `-s=salary --order=desc --stat -o=file --path=output\stat.txt`
        - `--stat`
          3.2 Или же настройте конфигурацию Application в диалоговом окне Run/Debug Comfigurations
4. Проверьте результаты в файле (`--path`) или консоли.

## Требования
- Доступ на запись в `output`.

- Корректные аргументы:
  --sort(-s)=name/salary
  --order
  --stat
  -output(-o)
  --path
  другие параметры являются ошибочными.

- Совместимость: Windows, Linux, macOS.

## Примечания
- Недопустимые значения (например, `-o=а`) переключают вывод в консоль.
- ошибочные параметры сортировки продолжат выполнение программы без сортировки.
- некорректный параметр статистики продолжит выполнение программы без вывода статистики
- Для отладки проверьте `main/resources/output/logs/error_details.log`.

## запуск производился с

OpenJDK Runtime Environment Temurin-17.0.16+8 (build 17.0.16+8)

Apache Maven 3.9.9

Apache Maven Daemon (mvnd) 1.0.2 windows-amd64 native client