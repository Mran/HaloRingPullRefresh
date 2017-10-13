## HaloRingRefreshLayout

在[UI Movement](https://uimovement.com/ui/4345/preloader-refresh/)上看到一个好看的下拉刷新的控件效果,想着把他实现了.
<!--more-->
### 效果预览
原地址在[UI Movement](https://uimovement.com/ui/4345/preloader-refresh/)
效果是这样的 

![](https://uimovement.com/media/resource_image/image_4345.gif)

下面是我做出来的效果

 
 ![](http://oe38oe3ti.bkt.clouddn.com/17-10-13/81807585.jpg)
 ### HOW to Use
 和 ``SwipeRefreshLayout``基本一样
```xml
    <com.mran.haloringpulltorefresh.HaloRingRefreshLayout
        android:id="@+id/haloring"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/colorb"
        app:headerHeight="120dp"
        app:ringRadius="20dp"
        app:ringTop="50dp">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/recyclerview"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
    </com.mran.haloringpulltorefresh.HaloRingRefreshLayout>
```
HaloRingRefreshLayout最多只能包含一个控件

#### 定制效果 

``ringRadius``圆环半径

``ringColor``圆环颜色

``ringTop``圆环距顶部的距离

``headerHeight`` 整个刷新动画部分的高度

``pointColor`` 形成圆环的点的颜色

#### 控制方法

`` mHaloRingRefreshLayout.setEnableRefresh(false);``//设置能否刷新

`` mHaloRingRefreshLayout.stopRefresh();``//取消刷新

`` mHaloRingRefreshLayout.isRefreshing();``//是否正在刷新


`` mHaloRingRefreshLayout.isBeingDragged();``//是否在拖动

`` mHaloRingRefreshLayout.isPullEnd();//``是否拖动到底部

`` mHaloRingRefreshLayout.getPercent();``//移动距离和可移动距离的百分比

``mHaloRingRefreshLayout.getMaxDragDistance();``//可移动的最大距离


解析参见我们博客

# License
[Apache](http://www.apache.org/licenses/LICENSE-2.0.html)

