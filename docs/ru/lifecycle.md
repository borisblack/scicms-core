# Жизненный цикл сущностей

SciCMS Core поддерживает три способа отслеживания (перехвата) жизненного цикла (ЖЦ) сущностей - хуки, вызов кастомных методов и продвижение (promotion).
Для использования первых двух сущность должна иметь имплементацию (класс-обработчик), которая наряду с кастомными методами реализует интерфейсы необходимых хуков.
Имя обработчика задается в поле `implementation`сущности и представляет собой полное имя Kotlin/Java класса (например, `ru.scisolutions.customimpl.item.PartItemImpl`).
Обработчик может быть простым Kotlin/Java классом, либо Spring-бином. В первом случае ядро самостоятельно создаст его экземпляр.
В коде обработчика-бина доступно большое количество сервисов (таких, как `ItemService`, `ItemRecDao`, `ACLItemRecDao` и др.).
Примеры хуков можно найти в пакетах [ru.scisolutions.customimpl.item](/src/main/kotlin/ru/scisolutions/customimpl/item), [ru.scisolutions.scicmscore.engine.hook](/src/main/kotlin/ru/scisolutions/scicmscore/engine/hook/impl).

## Хуки жизненного цикла

Для каждого метода жизненного цикла сущности существует соответствующий хук.
Ниже перечислены Kotlin-интерфейсы и соответствующие методы, которые необходимо реализовать обработчикам сущности.
Обработчик может имплементировать любое число хуков или вообще ни одного.
Ядро самостоятельно вызовет нужный метод в момент вызова соответствующей API-операции над сущностью.

### CreateHook

Содержит методы `beforeCreate` и `afterCreate`, которые вызываются для операции `create` соответственно перед и после сохранения новой записи для сущности.
```kotlin
interface CreateHook {
    /**
     * If this method returns not null, the engine will not create the data.
     */
    fun beforeCreate(itemName: String, input: CreateInput, data: ItemRec): ItemRec?

    fun afterCreate(itemName: String, response: Response)
}
```

Первый метод принимает имя сущности, входные параметры API-метода, и сформированную для сохранения запись.
Если метод сам возвращает объект записи, то ядро не будет выполнять ее сохранения в БД. В противном случае необходимо вернуть `null`.

Второй метод принимает имя сущности и объект ответа, который будет возвращен клиенту.

### CreateLocalizationHook

Содержит методы `beforeCreateLocalization` и `afterCreateLocalization`, которые вызываются для операции `createLocalization` соответственно перед и после сохранения новой локализации сущности.
Сигнатура методов аналогична `CreateHook`, за исключение типа возвращаемого значения метода `beforeCreateLocalization` - `Void`.

```kotlin
interface CreateLocalizationHook {
    fun beforeCreateLocalization(itemName: String, input: CreateLocalizationInput, data: ItemRec)

    fun afterCreateLocalization(itemName: String, response: Response)
}
```

### CreateVersionHook

Содержит методы `beforeCreateVersion` и `afterCreateVersion`, которые вызываются для операции`createVersion` соответственно перед и после сохранения новой версии сущности.
Сигнатура методов аналогична `CreateLocalizationHook`.

```kotlin
interface CreateVersionHook {
    fun beforeCreateVersion(itemName: String, input: CreateVersionInput, data: ItemRec)

    fun afterCreateVersion(itemName: String, response: Response)
}
```

### UpdateHook

Содержит методы `beforeUpdate` и `afterUpdate`, которые вызываются для операции `update` соответственно перед и после сохранения сущности.
Сигнатура методов аналогична `CreateHook`.
```kotlin
interface UpdateHook {
    /**
     * If this method returns not null, the engine will not update the data.
     */
    fun beforeUpdate(itemName: String, input: UpdateInput, data: ItemRec): ItemRec?

    fun afterUpdate(itemName: String, response: Response)
}
```

### DeleteHook

Содержит методы `beforeDelete` и `afterDelete`, которые вызываются для операции `delete` соответственно перед и после удаления сущности.
```kotlin
interface DeleteHook {
    fun beforeDelete(itemName: String, input: DeleteInput, data: ItemRec)

    fun afterDelete(itemName: String, response: Response)
}
```

Первый метод принимает имя сущности, входные параметры API-метода, и удаляемую запись.
Второй метод принимает имя сущности и объект ответа, который будет возвращен клиенту.

### PurgeHook

Содержит методы `beforePurge` и `afterPurge`, которые вызываются для операции `purge` соответственно перед и после удаления всех версий сущности.
Сигнатура методов аналогична `DeleteHook`. Третьим параметром метода `beforePurge` передается текущая версия удаляемой записи.
```kotlin
interface PurgeHook {
    fun beforePurge(itemName: String, input: DeleteInput, data: ItemRec)

    fun afterPurge(itemName: String, response: ResponseCollection)
}
```

### LockHook

Содержит методы `beforeLock`, `afterLock`, `beforeUnlock` и `afterUnlock`. Первая пара методов вызывается для операций `lock`, вторая - для операций `unlock` соответственно перед и после блокировки/разблокировки сущности.
```kotlin
interface LockHook {
    fun beforeLock(itemName: String, id: String, data: ItemRec)

    fun afterLock(itemName: String, response: FlaggedResponse)

    fun beforeUnlock(itemName: String, id: String, data: ItemRec)

    fun afterUnlock(itemName: String, response: FlaggedResponse)
}
```

Методы `beforeLock` и `beforeUnlock` принимают имя сущности, входные параметры API-метода, и блокируемую/разблокируемую запись.
Методы `afterLock` и `afterUnlock` принимают имя сущности и объект ответа, который будет возвращен клиенту.

### FindOneHook

Содержит методы `beforeFindOne` и `afterFindOne`, которые вызываются соответственно перед и после операции получения данных сущности (имя операции соответствует имени сущности).
```kotlin
interface FindOneHook {
    fun beforeFindOne(itemName: String, id: String)

    fun afterFindOne(itemName: String, response: Response)
}
```

Первый метод принимает имя сущности и идентификатор записи.
Второй метод принимает имя сущности и объект ответа, который будет возвращен клиенту.

### FindAllHook

Содержит методы `beforeFindAll` и `afterFindAll`, которые вызываются для соответственно перед и после операции поиска сущностей (имя операции соответствует имени сущности во мн. числе).
```kotlin
interface FindAllHook {
    fun beforeFindAll(itemName: String, input: FindAllInput)

    fun afterFindAll(itemName: String, response: ResponseCollection)
}
```

Первый метод принимает имя сущности и входные параметры API-метода.
Второй метод принимает имя сущности и объект ответа, который будет возвращен клиенту.

### GenerateIdHook

Содержит единственный метод `generateId`, который вызывается для генерации идентификатора записи в методах `create`, `createVersion` и `createLocalization` (по умолчанию в качестве идентификаторов используется строка формата [UUID](https://en.wikipedia.org/wiki/Universally_unique_identifier)).

```kotlin
interface GenerateIdHook {
    fun generateId(itemName: String): String
}
```

Метод принимает имя сущности в качестве единственного параметра.

## Кастомные методы

Помимо имплементации интерфейсов хуков обработчик сущности может иметь неограниченное число методов в качестве GraphQL операций.
В схеме GraphQl эти методы будут дополнены именем сущности (с заглавной буквы) в качестве суффикса. Методы должны соответствовать определенным правилам:
- иметь модификатор `public`;
- не совпадать с зарезервированными именами методов хуков;
- в качестве единственного входного параметра принимать тип `CustomMethodInput`;
- возвращаемым типом должен быть `CustomMethodResponse`.

Классы `CustomMethodInput` и `CustomMethodResponse` являются простыми обертками над Kotlin типом `Any?` (`Object` в Java):

```kotlin
class CustomMethodInput(
    val data: Any? = null
)

class CustomMethodResponse(
    val data: Any? = null
)
```

Пример кастомного метода можно найти в классе [PartItemImpl](/src/main/kotlin/ru/scisolutions/customimpl/item/PartItemImpl.kt).

## Продвижение (promotion)

Помимо имплементации, каждая сущность имеет свойство `lifecycle`.
Это свойство содержит идентификатор записи другой сущности с именем `lifecycle`, которая хранится в основной БД в таблице `core_lifecycles`.
Эта вторая сущность содержит спецификацию жизненного цикла (последовательность этапов) и поле `implementation` с полным именем класса-обработчика, который должен реализовать интерфейс `Promotable` с единственным методом `promote`:
```kotlin
interface Promotable {
    fun promote(itemName: String, id: String, state: String)
}
```

Метод принимает имя сущности, идентификатор записи и строковое значение нового этапа жизненного цикла.
Как и в случае с обработчиками сущности, обработчик ЖЦ может быть простым Kotlin/Java классом, либо Spring-бином.
В первом случае ядро самостоятельно создаст его экземпляр. В коде обработчика-бина доступно большое количество сервисов.
Пример обработчика можно найти в пакете [ru.scisolutions.customimpl.lifecycle](/src/main/kotlin/ru/scisolutions/customimpl/lifecycle).

Спецификация ЖЦ представляет собой XML-строку в нотации BPMN и может быть открыта в совместимом редакторе.
Клиентское приложение [SciCMS Client](https://github.com/borisblack/scicms-client) использует для этих целей библиотеку [diagram-js](https://github.com/bpmn-io/diagram-js):

![Спецификация жизненного цикла сущности](/docs/img/lifecycle.png "Спецификация жизненного цикла сущности")

В момент выполнения метода `promote` над записью через GraphQL API вызывается одноименный метод обработчика ЖЦ, который, в свою очередь, может выполнять любую бизнес-логику (отправки оповещений, постановка заданий в очередь, запись в БД и пр.).
Значение нового этапа ЖЦ сохраняется в поле `state` записи.

Клиентское приложение [SciCMS Client](https://github.com/borisblack/scicms-client) предоставляет удобный пользовательский интерфейс для редактирования спецификаций ЖЦ и выполнения продвижений, а также многие другие функции.
