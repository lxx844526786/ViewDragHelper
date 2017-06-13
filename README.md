# ViewDragHelper
自定义侧滑菜单

	1.因为这个控件有两部分,菜单界面和主界面,所以自定义控件一定是ViewGroup.所以先定义一个SlideMenu继承Framelayout,因为Framelayout是最轻量级的布局,而且FrameLayout已经实现了onMeasure()方法.
	
	2.	要去除ActionBar,在Values文件夹下的styles文件里AppTheme更改theme主题为NoActionBar.
		沉浸式状态栏,也就是状态栏透明,API19以上,把statusBarColor属性改为@android:color/transparent.
	3.因为需要手指拖动时候,两个子view需要进行移动,所以需要用到ViewDragHelper类,它是谷歌13年推出的帮助我们在ViewGroup中进行子view的移动.它是在4.4以上的v4包中,它的本质是对触摸事件的解析类,所以我们还要给它传递触摸事件.

	4.我们需要在onFinishInflate()方法中得到主界面和菜单界面,因为这个方法是ViewGroup将子view全部添加完毕之后执行,在onMeasure方法之前执行.一般用来初始化子view,但是不能获取子view的宽高.
	我们可以在onSizeChanged()方法中得到mainView和menuView的宽和高,得到限定的距离dragRange.

	5.我们需要在构造方法中对ViewDragHelper进行初始化,定义一个init()方法,在方法里对ViewDragHelper进行初始化,它有一个静态的方法,create()里面有两个参数,第一参数是当前的ViewGroup,第二个参数是viewDragHelper的回调函数;
	callBack里面需要重写几个方法

	第一个:tryCaptureView 
		是否对子view进行捕获,
		判断childView 是否等于两个子view
	第二个:getViewHorizontalDragRange 
		水平方向理论的移动范围,如果子view要被消费,必须返回一个大于0的值
	第三个:clampViewPositionHorizontal
		修正或者指定view在水平方向上移动的距离
		返回的值是我们真正想让view移动的距离,在这里我们给的最大距离时宽的0.6倍
	第四个:onViewPositionChanged
		当view发生改变时调用的方法
		1.	当菜单移动时,主界面伴随移动,并且菜单界面保持不动,
		首先判断childView == menuView,然后用menuView.layout()方法保持menuView的位置不动,
		然后计算出新的left,newLeft = mainView.getLeft() + dx;
		再限制移动范围,当newLeft大于dragRange时,让newLeft等于dragRange,小于0时,让newLeft等于0;
		然后调用mainView的layout方法重新摆放下
		2.在mainView拖动的时候执行伴随动画,首先拿到主界面移动时与dragRange的百分比
		根据百分比刷新当前的mainview的状态
		3.	增加一个接口,给外界调用,当侧滑界面打开时,当侧滑界面关闭时,当侧滑界面拖动时,调用什么逻辑.在主界面中调用slideMenu实现这个接口的方法,主界面有一个顶部头像,我们可以让它在拖动的时候,进行渐变动画,还是需要用到估计值.
		view.setAlpha(FloatEvaluator.evalute(fraction,1f,0)); 从显示到隐藏
	第五个:onViewReleased
		当view释放时调用的方法
		当手指松开时,让菜单界面自己滑动归位.
		首先判断mainView.getLeft是否大于dragRange(限定距离)的一半,大于的话滑向最右边,小于的话滑向最左边.
		需要用到API,smoothSlideViewTo(),然后执行刷新操作
		ViewCompat.postInvalidateOnAnimation();
		这时需要重写一个computeScroll方法,判断dragHelper是否还在执行动画,是的话就执行刷新操作.
		 //如果还在执行动画
		if (dragHelper.continueSetting(true)) {
			//刷新
		ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
				}
		}

	##在侧滑打开的时候,点击主界面可以让关闭,并且主界面上下,左右不可滑动	  
		主界面的布局是一个自定义的LinearLayout,我们在自定义的LinearLayout里面重写dispatchTouchEvent方法.
		首先我们需要在SlideMenu里面提供一个方法返回当前的侧滑菜单的状态.
		在dispatchTouchEvent方法里判断侧滑菜单的状态是打开状态的时候,请求父控件不要拦截事件,就是getParent().requestDisallowInterceptTouchEvent(true);
		然后判断手指落下时的坐标和手指抬起时候的坐标是否相等,相等的话就代表是点击事件,就让SlideMenu关闭.不相等的话就代表是滑动事件,就不处理滑动事件.

