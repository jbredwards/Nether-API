# Nether API

---

### Info For Mod Devs

##### Add the following to your `build.gradle` to add this mod's files to your workspace:

```groovy
dependencies {
    deobfCompile 'com.github.jbredwards:nether-API:f2f48d7b8b'
}

repositories {
    maven { url 'https://jitpack.io' }
}
```

##### Any mods using this as a dependancy must make sure they're using stable_39 mappings for this mod to work properly in a deobfuscated enviornment!
