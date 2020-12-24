# SpongeIntegrationPlugin
Плагин интеграции Sponge в личным кабинетом SimpleCabinet  
Для работы нового API пинга сервера праивльно укажите serverName в ServerWrapper(необязательно)  
**Привязка с помощью ServerWrapper обязательна**
## Формат предметов
- ItemId может принимать только строковое значение `minecraft:diamond_sword`/`ic2:itemcoin`
- ItemExtra может принимать только числовое значение, не превышающее 65535(short) или `null`
- ItemNbt пишется в формате mojangson, как в `/give`
- Название зачарования пишется только в строковом формате `minecraft:luck`
## Сборка
Выполните `./gradlew build`
