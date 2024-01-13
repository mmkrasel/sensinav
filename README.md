# sensinav
**Exploit Smartphone Sensors for Indoor Navigation**

At first, we need to configure some information for using SensInav as a library to other app or customizing the features of this app. For example, to communicate with the remote side server script, _HOME_URL_ must be updated at _util/Util.java_ as below.

```java
  ....
  private String HOME_URL = "_URL_TO_SERVER_SIDE_SCRIPT_";
  ....
```

The default value of _HOME_URL_, a _building name_, and the _lowest_ and the _highest_ floor of a building can be updated at the _settings()_ method of _NavConstructorActivity_ class.

```java
....
private void settings(){
    Intent i = this.getIntent();
    if(i.hasExtra("URL")){
        String url = i.getStringExtra("URL");
        Util.getInstance().setUrl(url);
    }
    .....
    if(i.hasExtra("BUILDING_NAME")){
        String bn = i.getStringExtra("BUILDING_NAME");
        MapInfo.getInstance().buildingName = bn;
        ......
    }
    if(i.hasExtra("LOWEST_FLOOR")){
        fromFloor = i.getIntExtra("LOWEST_FLOOR", 0);
    }
    if(i.hasExtra("HIGHEST_FLOOR")){
        toFloor = i.getIntExtra("HIGHEST_FLOOR", 30);
    }
}
....
```

The _default value_ and _threshold_ for the _walking step _distance__ are 0.6 meter and 0.8 meter, respectively. On the otherhand, the _threshold_ value for gyroscope reading is 1.0f. However, these values can be updated according to the following code segment.
```java
....
MapInfo.getInstance().STEP_DISTANCE = _NEW_VALUE_;
MapInfo.getInstance().STEP_THRESHOLD = _NEW_VALUE_;
MapInfo.getInstance().GYROSCOPE_THRESHOLD = _NEW_VALUE_;
....
```
