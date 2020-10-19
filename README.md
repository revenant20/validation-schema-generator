# validation-schema-generator
Для того что бы поучить схему валидации надо:

1. В файл `schemas/body.json` вставить тело вашего запроса или ответа
2. В методе `Main.main()` при использовании `parser.pars()` указать нужный вам `MessageType`.
3. Запустить программу, схема валидации сгенерится в файл `schemas/validation-schema.json`