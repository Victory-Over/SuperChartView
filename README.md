# SuperChartView

#### 1、添加依赖和配置

* 根目录build.gradle文件添加如下配置：

```Java
allprojects {
    repositories {
       	maven { url 'https://jitpack.io' }
    }
}
```

* APP目录build.gradle文件添加如下配置：

```Java
dependencies {
    'com.github.Victory-Over:SuperChartView:v1.0.0'
}
```

#### 2、效果展示
![点我查看效果图](https://github.com/Victory-Over/Resource/blob/master/file_chartview.gif)

#### 3、核心代码

####### 每次滚动完成 计算滚动的位置，使indicate居中并回调当前位置的position 供外部使用
```Java
    /**
     * 调整indicate，使其居中。
     */
    private void adjustIndicate() {
        if (!mOverScroller.isFinished()) {
            mOverScroller.abortAnimation();
        }

        int position = computeSelectedPosition();
        int scrollX = getScrollByPosition(position);
        scrollX -= getScrollX();
        this.position = position;

        if (scrollX != 0) {
            mOverScroller.startScroll(getScrollX(), getScrollY(), scrollX, 0);
            invalidateView();
        }

        //滚动完毕回调
        onScaleChanged(position);
    }
```
