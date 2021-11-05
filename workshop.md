# Workshop

This is a branch for starting the Workshop after the Kaspresso part of the lecture.

## Agenda

This workshop consists of 3 main parts:
1) Launch `SampleTest` to ensure, you have set up the environment for launching UI-tests correctly.
2) Writing PageObject (KScreen in terms of Kaspresso) for existing test steps. Have a passing test as an outcome.
3) Writing test actions for existing PageObject and existing assistant actions. Have also the passed test as an outcome.

## Part1: Launch SampleTest

### Open `SampleTest.kt`
To find the test file you can either
- Tap `Cmd+O` \ `Ctrl+N` \ `Double Shift + go to Classes tab ` and start typing `SampleTest` to find it by ClassName
- Tap `Cmd+Shift+O` \ `Ctrl+Shist+N` \ `Double Shift + go to Files tab ` and start typing `SampleTest.kt` to find it by FileName

Then try to tap on *double green arrow* to run the test (see screenshot beyond)

![img_1.png](images/img_1.png)

If you've got `Test Passed` (see screenshot beyond) - you are lucky - go to the next part. Otherwise, see the `Setup Emulator` section.

![img_2.png](images/img_2.png)

### Setup Emulator

- Open Device Manager
![img_3.png](images/img_3.png)
- If there are no setup devices tap `Create Device` button
![img_4.png](images/img_4.png)
- Choose some common device like `Pixel 4`. Tap `Next`
![img_5.png](images/img_5.png)
- Choose some last system version like `R (Android 11)`. Tap `Next`
![img_6.png](images/img_6.png)
- Tap `Finish`
![img_7.png](images/img_7.png)

Now you should have this Device in `Device Manager` and be able to launch SampleTest.

If not – ask about your problem in the chat of Android Academy.

## Part 1: Writing PageObject

At this point, you should have `SampleTest` running and Green (`Test Passed `)

This workshop is structured similarly to TDD (Test Driven Development) technique. First, we will make our code Red and not compile, then reach a Green test.

To start, uncomment code inside steps `step("Check film details screen")` and `step("Open Login")` of `SampleTest.kt`

You should have:

```kotlin
// Workshop part 1 - writing PageObject
// To complete you need
// 1. Uncomment code inside this step - it will be red at first
// 2. Create a `MovieScreen` class - page object for `MovieDetailsFragment` - it should make test passing
step("Check film details screen") {
    MovieScreen {
        title {
            isVisible()
            hasText("Duna")
        }
        description {
            isVisible()
        }
        image {
            isVisible()
        }
    }
}
// 3. Uncomment code inside this step - it will be red at first
// 4. Add `loginButton` to MovieScreen - it should make test passing
step("Open Login") {
    MovieScreen {
        loginButton {
            isVisible()
            click()
        }
    }
}
```

For now, it is Ok, that it is all red.

Let's make it green.

### Create a `MovieScreen` class - page object for `MovieDetailsFragment` - it should make test passing

At this step, we will write a PageObject (KScreen in terms of Kaspresso) for `MovieDetailsFragment` to make the test for this screen passing.

1) Find `MainScreen.kt`, see its structure if forgotten from the lecture.
2) Put the new `MovieScreen.kt` file in the same directory.
3) Make `MovieScreen` class extending `KScreen<MainScreen>`
4) Override necessary values for `KScreen`: `layoutId` and `viewClass`. These are some pointers to help support this class in the future.
5) For our case `layoutId` should have `R.layout.movies_fragment` and `viewClass` link to `MoviesFragment::class.java`
6) Return to `SampleTest.kt`. Now `MovieScreen` should not be red anymore except elements.
7) Return to `MovieScreen.kt`. Define elements inside related to the views and their ids from `R.layout.movies_fragment`.
   1) The simplest way is to copy `val someElement = KView { withId(R.id.someId) }` and change names accordingly.
   2) Name element like it is intended in `SampleTest.kt`: `title`, `description` or `image`.
   3) Replace `KView` by class of View plus `K` at the beginning (mostly it works). 
   Thus for `TextView` Kakao has `KTextView`, for `ImageView` - `KImageView`, ... If you can't find analog, you can retain `KView`.
   4) Replace `someId` inside of matcher `withId(R.id.someId)` with actual id of a view. 

   At this point you should have something like `val title = KTextView { withId(R.id.tv_movie_details_title) }`.
   5) You will also need to add matcher `withText(R.string.someStringId` to distinct `loginButton` and `addReview` with same ids in next steps.
   
   For example:
```kotlin
val loginButton = KView {
   withId(R.id.tv_review_add_label_label)
   withText(R.string.authiorize_to_add_review_label)
}
```
8) Return to `SampleTest.kt`. Now the code inside `MovieScreen` should not be red at all.
9) Launch test. Check it passed.

If not, you can compare your `MovieScreen.kt` with a [solution file](https://github.com/Android-Academy-Global/advanced-2021-architecture-1/blob/kaspresso-solution/app/src/androidTest/java/MovieScreen.kt) or ask a question in a chat.

## Part 2: Completing the Test

At this step, we will write remained test actions for existing PageObject and existing assistant actions.

To start, uncomment code inside steps `step("Login")` and `step("Check film details screen contains `Add review`")` of `SampleTest.kt`

You should have:

```kotlin
// Workshop part 2 - writing Test
// To complete you need
// 1. Uncomment existing code inside this step.
// There was intentionally left spaces to fill them and make test passing.
step("Login") {
    AuthScreen {
        // TODO
        authButton.click()
        emailInput.hasError(R.string.email_input_error)
        // TODO
        authButton.click()
        passwordInput.hasError(R.string.password_input_error)
        // TODO
        authButton.click()
        // the login here should be completed, however we will check it in the next step
    }
}
// 2. Uncomment existing code inside this step.
// 3. Add `addReview` to MovieScreen - it should make test passing
step("Check film details screen contains `Add review`") {
    MovieScreen {
        addReview {
            isVisible()
            containsText("Add review")
        }
    }
}
```

For now, it is Ok, that it is all red.

Let's make it green.

1) In `MovieScreen.kt` add `addReview` element, if not in previous part. See step `7-5` above. It should be like this:
```kotlin
 val addReview = KTextView {
     withId(R.id.tv_review_add_label_label)
     withText(R.string.review_add_label)
 }
```
2) Now code should not be red. You can try launching the test, but it will not pass.
3) To make it pass, you should replace all `TODO()`
4) Check `AuthScreen.kt` there are already elements, that should help you.
5) `\\1 TODO` should be replaced with putting some not-email text into `emailInput`. 
For that we can simply write `emailInput.edit.typeText("test")`
6) Launch test, make sure, the text is typing, but the test is failing.
7) `\\2 TODO` should be replaced with clearing `emailInput` and putting some email. 
For that we can use actions `clearText()` and `typeText("test")` over `edit`.
8) Launch test, make sure, the text is typing, clearing, then email typing, but the test is still failing.
9) `\\3 TODO` should be replaced with putting some password text into `passwordInput`.
You probably know already how to do it.
10) Launch test. Check it passed.

If not, you can compare your `SampleTest.kt` with a [solution file](https://github.com/Android-Academy-Global/advanced-2021-architecture-1/blob/kaspresso-solution/app/src/androidTest/java/FullTest.kt) or ask a question in a chat.

# Summary
That's all. You completed the first Kaspresso test for this app.
You can try to cover other screens and scenarios or deep dive into [Kaspresso possibilities](https://github.com/KasperskyLab/Kaspresso/blob/master/wiki/00_Home.md).