# Workshop
This is the branch for the workshop after the Linters part of the lecture.

## Agenda
This workshop consists of the three main parts:
1. Lint configuration;
2. Writing custom Lint rule;
3. Testing custom Lint rule.

## Part 1: Lint configuration
Lint is already there in the project as part of AGP. While discovering the project, you could notice that Lint rule violations already exist in it. We're going to enforce the policy, which prevent the number of violations from growing, and also try to handle some existing issues. "Poehali!"

- Build the application by executing the `./gradlew :app:build` command in the terminal. Observe that build is successful;
- Observe that Lint reports are produced. That's because `build` task depends on `lint` task by default:
  ![img_1.png](images/img_1.png)
- Run the Lint via `./gradlew :app:lint` command in the terminal. You should see the same report:
  ![img_2.png](images/img_2.png)
- Open the `lint-results.xml` file by the link from the console output and investigate the issues found. There must be 17 issues;
- The build succeeds because all those issues has `warning` severity. However, for the best product quality those should be addressed;
- First of all, lets prevent the number of issues from growing. Open `app/build.gradle` file and create `lintOptions` section inside of the `android` section:
  ```gradle
  android {
    lintOptions {
    }
  }
  ```
- Set `warningsAsErrors` property within it to `true`:
  ```gradle
  android {
    lintOptions {
        warningsAsErrors true
    }
  }
  ```
- Build the application by executing the `./gradlew :app:build` command in the terminal. Observe that build fails:
  ![img_5.png](images/img_5.png)
- Now lets make the build pass again;
- Lets fix the `UnusedResources`issue. There must be 5 occurrences of it in the report. Just delete the referenced resources:
  ![img_6.png](images/img_6.png)
- Fix the `IconLocation` issue. Issue description suggests moving the referenced image into `drawable-nodpi` folder. Do it:
  ![img_7.png](images/img_7.png)
  ![img_8.png](images/img_8.png)
- Fix the `Autofill` issue. We don't need auto fill, so fix it by one of the ways the rule suggests:
  ![img_9.png](images/img_9.png)
- Run the build again. Observe that report has become smaller by 7 issues:
  ![img_10.png](images/img_10.png)
- Lets imagine that we can't fix the `VectorPath` issue as nobody can provide us new resource. It means that we should suppress it. Open the file, referenced in the issue, and add `tools` namespace into it. Just start typing "tools" and you should see the quick suggestion from the Android Studio:
  ![img_11.png](images/img_11.png)
- Add `tools:ignore="VectorPath"` attribute into the root tag of the image:
  ![img_12.png](images/img_12.png)
- `Overdraw` issue might be hard to fix as it might require changing the design system. Also, in some cases the reporting of this issue is false-positive. Anyway, we don't want to **ever** fix it. Lets disable this rule;
- Open `app/build.gradle` file and navigate to the `lintOptions` section. Add there `disable 'Overdraw'` property:
  ```gradle
  android {
    lintOptions {
        warningsAsErrors true
        disable 'Overdraw'
    }
  }
  ```
- Run the build again. Only 7 issues blocking the successful build!
  ![img_14.png](images/img_14.png)
- There're accessibility related and backup related issues left. We can't do it right now, because we don't have necessary string resources and don't know how our product should handle backup. But we definitely want them to be addressed **in the future** and we also want new code to not violate those rules. It means that we **must not** suppress those issues or disable the related rule. We should put them into the `lint-baseline.xml` file and fix later;
- Open `app/build.gradle` file and navigate to the `lintOptions` section. Add there `baseline file("lint-baseline.xml")` property:
  ```gradle
  android {
    lintOptions {
        warningsAsErrors true
        disable 'Overdraw'
        baseline file("lint-baseline.xml")
    }
  }
  ```
- Run the build again. Observe it fails. Read the failure reason carefully:
  ![img_16.png](images/img_16.png)
- Observe `lint-baseline.xml` file created in the `app` module. Check that it has the content similar to the `lint-result.xml`:
  ![img_17.png](images/img_17.png)
- Run the build again. Observe it passes:
  ![img_18.png](images/img_18.png)
- Commit all changes including the `lint-baseline.xml` into the repository.

## Part 2: Custom Lint rules
In our project all dimensions are set through the dimen resources. All except of the text sizes. We'll create a lint rule to warn developers about the hardcoded text size usage.
- Create new Kotlin module using "new module" wizard in Android Studio. Call module `lint-ruleset`, package `com.example.ruleset` and main class `HardcodedTextSizeUsageDetector`:
  ![img_2_1.png](images/img_2_1.png)
  ![img_2_2.png](images/img_2_2.png)
- Go to the `build.gradle` file of the new module and leave there only Kotlin plugin:
  ```gradle
  plugins {
    id 'kotlin'
  }
  ```
- In the same file add the dependency to the lint api. Notice, that the version must be equal to the AGP version +23:
  ```gradle
  dependencies {
    compileOnly "com.android.tools.lint:lint-api:27.2.2"
  }
  ```
- Open `HardcodedTextSizeUsageDetector` file. Make the class extend `LayoutDetector`:
  ```kotlin
  internal class HardcodedTextSizeUsageDetector : LayoutDetector()
  ```
- Create and configure issue. You can setup all fields as you wish except of the `implementation`:
  ```kotlin
  internal val HardcodedTextSizeUsageIssue = Issue.create(
    id = "HardcodedTextSizeUsage",
    briefDescription = "Hardcoded text size values are forbidden.",
    explanation = "Hardcoded text size values are forbidden. Please, replace with a dimen resource.",
    category = Category.TYPOGRAPHY,
    severity = Severity.WARNING,
    implementation = Implementation(
        HardcodedTextSizeUsageDetector::class.java,
        Scope.RESOURCE_FILE_SCOPE,
    ),
    androidSpecific = true,
  )
  ```
- Make the detector visit `textSize` attributes of the view. To do that, override the `getApplicableElements` method:
  ```kotlin
  override fun getApplicableAttributes(): Collection<String> {
    return listOf(SdkConstants.ATTR_TEXT_SIZE)
  }
  ```
- Implement attribute visitor by overriding `visitAttribute` method. We don't need to process `textSize` attributes not from `android` namespace and we want to report the issue whenever the value of an attribute does not reference any resource:
  ```kotlin
  private val hardcodedResourcePattern = Pattern.compile("^\\d+(sp|dp|px)\$")
  
  override fun visitAttribute(context: XmlContext, attribute: Attr) {
    if (attribute.namespaceURI != SdkConstants.ANDROID_URI) return
    
    if (hardcodedResourcePattern.matcher(attribute.value).matches()) {
        context.report(
            issue = HardcodedTextSizeUsageIssue,
            location = context.getValueLocation(attribute),
            message = "Using hardcoded text sizes is forbidden.",
        )
    }
  }
  ```
- It's time to create `IssueRegistry`. Create the class caleld `ExampleIssueRegistry`, inherit it from the `IssueRegistry` class and override properties `api` and `issues`. It is important to override `api` property with the `com.android.tools.lint.detector.api.CURRENT_API` value, otherwise you'll get `ObsoleteLintCustomCheck` warning reported by the Lint:
  ```kotlin
  internal class ExampleIssueRegistry : IssueRegistry() {
  
    override val api = CURRENT_API

    override val issues = listOf(HardcodedTextSizeUsageIssue)
  }
  ```
- Add your service registry into the Java service locator. Open `lint-rules/build.gradle` file and add there the following lines:
  ```gradle
  tasks.withType(Jar) {
    manifest.attributes["Lint-Registry-v2"] = "com.example.ruleset.ExampleIssueRegistry"
  }
  ```
- Add the custom checks to your project. Open `app/build.gradle` and add there one more dependency:
  ```gradle
  dependencies {
    lintChecks project(":lint-ruleset")
  }
  ```
- Run the build. Observe it failing due to the violations of our custom rule:
  ![img_2_12.png](images/img_2_12.png)
- Open one of the files, which violates the rule and observe issue highlighting in editor:
  ![img_2_13.png](images/img_2_13.png)

## Part 3: Testing custom Lint rules
In this part we're going to test the rule, which we've just written.

>> If test written in this part fails due to the "absent SDK" please setup ANDROID_HOME environment variable and point it to the Android SDK root folder.

- Configure Lint test dependencies in `lint-ruleset` module:
  ```gradle
  dependencies {
    testImplementation "com.android.tools.lint:lint-tests:27.2.2"
    testImplementation "com.android.tools.lint:lint:27.2.2"
  }
  ```
- We'll use JUnit5 (Jupiter Platform) as a testing framework. Add the necessary dependencies into the `lint-ruleset` module and enable the `junitPlatform` in it:
  ```gradle
  dependencies {
    testImplementation "org.junit.jupiter:junit-jupiter-api:5.8.1"
    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:5.8.1"
  }
  ```
  ```gradle
  tasks.withType(Test) {
    useJUnitPlatform()
  }
  ```
- You can try using JUnit4 on your own;
- We'll test the scenario when resource value is used and thus no rule violation reported. Create the test file in the `/lint-ruleset/src/test/com/example/ruleset` directory and add the test method into it. Give it meaningful name!
  ```kotlin
  internal class HardcodedTextSizeUsageDetectorTest {
  
    @Test
    fun `given resource is used in textSize when detector applies then HardcodedTextSizeUsageIssue is not reported`() {
    }
  }
  ```
- Prepare the test data. In our case it'll be an XML file. Use `TestFiles#xml` helper function to create it:
  ```kotlin
  val layout = xml(
    "res/layout/activity_main.xml",
    """
        <TextView xmlns:android="http://schemas.android.com/apk/res/android"
                android:textSize="@dimen/textSizeBig"/>
    """
  ).indented()
  ```
- Now, lets configure a `TestLintTask`. Create it using `TestLintTask#lint` function, feed it the xml file, which you've just created. Then pass in the `HardcodedTextSizeUsageIssue` - it is good practice to test a single rule per test:
  ```kotlin
  lint()
    .files(layout)
    .issues(HardcodedTextSizeUsageIssue)
  ```
- Run the task and expect it to be clean:
  ```kotlin
  lint()
    .files(layout)
    .issues(HardcodedTextSizeUsageIssue)
    .run()
    .expectClean()
  ```
- The final solution should look like this:
  ```kotlin
  @Test
    fun `given resource is used in textSize when detector applies then HardcodedTextSizeUsageIssue is not reported`() {
        val layout = xml(
            "res/layout/activity_main.xml",
            """
                <TextView xmlns:android="http://schemas.android.com/apk/res/android"
                    android:textSize="@dimen/textSizeBig"/>
            """
        ).indented()

        lint()
            .files(layout)
            .issues(HardcodedTextSizeUsageIssue)
            .run()
            .expectClean()
    }
  ```
- Execute the test and check that it works;
- Cover the "rule violation" scenario by yourself.