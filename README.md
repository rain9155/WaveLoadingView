# WaveLoadingView
### 一个类似于波浪效果的Loading控件，如有问题欢迎[issue](https://github.com/rain9155/WaveLoadingView/issues)

### 1、Pre

不再满足如Android原生的加载中控件，利用贝塞尔曲线原理，自己动手实现的一个加载中控件，实现原理查看，实现效果如下**Preview**，

### 2、Preview
![waveloading1.gif](/screenshots/waveloading1.gif)
### 3、Download

[地址](https://github.com/rain9155/WaveLoadingView/releases/download/1.0.0/app-release-unsigned.apk)

### 4、How to install？

在app目录下的build.gradle添加依赖，如下：

```
dependencies {
    implementation 'com.jianyu:waveloadingview:1.0.1'
}

```

### 5、How to use？

在布局中直接引用，如下：

```xml
    <com.example.library.WaveLoadingView
        android:id="@+id/wl"
        android:layout_width="200dp"
        android:layout_height="200dp"/>
```

当然直接引用都是使用默认的属性，你可以通过**wl_XX**前缀来自定义WaveLoadingView的其他属性，如颜色、形状等，所有支持的属性在下方的Attrs表中都有说明。

Attrs表中的属性都提供了对应的get/set方法，所以你可以直接在代码中自定义WaveLoadingView的其他属性，如下：

java写法：

```java
WaveLoadingView wl = findViewById(R.id.wl);
wl.setShape(WaveLoadingView.Shape.RECT);
wl.setWaveColor(Color.BLACK);
wl.setWaveBackgroundColor(Color.WHITE);
wl.setText("rain");
wl.setTextLocation(WaveLoadingView.Location.CENTER);
//...
```

Kotlin写法：

```kotlin
wl.shape = WaveLoadingView.Shape.RECT
wl.waveColor = Color.BLACK
wl.waveBackgroundColor = Color.WHITE
wl.text = "rain"
wl.textLocation = WaveLoadingView.Location.CENTER
//...
```

在有需要时(如Activity在后台)，你可以通过以下方法暂停、恢复、取消、启动loading动画，如下：

```kotlin
wl.pauseLoading()//暂停loading
wl.resumeLoading()//恢复loading
wl.cancelLoading()//取消loading
wl.startLoading()//启动loading
```



#### Attrs

|          名称          |                             说明                             |
| :--------------------: | :----------------------------------------------------------: |
|        wl_shape        | 边框形状，有4种取值：circle、square、rect、none，默认是circle，即圆形，none是没有形状约束 |
|     wl_shapeCorner     |    边框圆角，当 wl_shape = square或rect 才生效，默认是0dp    |
|      wl_waveColor      |                           波浪颜色                           |
| wl_waveBackgroundColor |                        波浪的背景颜色                        |
|    wl_waveAmplitude    |              波峰，取值范围是0~0.9f，默认是0.2f              |
|    wl_waveVelocity     |        波浪水平移动的速度，取值范围是0~1f，默认是0.5f        |
|     wl_borderColor     |                           边框颜色                           |
|     wl_borderWidth     |               边框宽度，默认是0dp，即没有边框                |
|       wl_process       | 波浪占整个控件的比例，取值范围是0~100，默认是50，即波浪占整个控件的一半 |
|        wl_text         |                  加载控件中的文字，默认为空                  |
|    wl_textLocation     | 文字在控件中的位置，有3种取值：flow、center、top、bottom，默认是flow，即漂浮在波浪上面 |
|      wl_textColor      |                           文字颜色                           |
|      wl_textSize       |                           文字大小                           |
|   wl_textStrokeWidth   |           文字边框大小，默认为0dp，即没有文字边框            |
|   wl_textStrokeColor   |                         文字边框颜色                         |
|      wl_textBold       |                       文字是否使用粗体                       |
|      wl_textWave       |                   文字是否跟随波浪上下浮动                   |


## License
```
Copyright 2019 rain9155

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License a

          http://www.apache.org/licenses/LICENSE-2.0 
          
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
