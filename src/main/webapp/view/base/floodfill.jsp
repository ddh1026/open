<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@include file="/module/include/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <sys:header title="首页" extLibs=""></sys:header>
	<link rel="stylesheet" href="${ctxStatic}/plugins/bootstrap-slider/slider.css">
    <script type="text/javascript">
		$(function(){
			var baseImageFile = "/statics/sourceimage/floodfill3.png"
			var newImagePath = "/statics/distimage/floodfill.png"
			$("#oldimg").attr("src",baseUrl+baseImageFile);
			//$("#newimg").attr("src",baseUrl+baseImageFile); 
			
			//锐化
			$("#floodfill").click(function(){
				var graysize = $("#graysize").val();
				var lodiff = $("#lodiff").val();
				var updiff = $("#updiff").val();
				var flag = $("#flag").val();
				//alert(graysize+"|"+lodiff+"|"+updiff+"|"+flag);
				var imagefile = baseImageFile;
				var srcurl = ctxPath+"/base/floodfill?_" + $.now()+"&graysize="+graysize+"&lodiff="+lodiff+"&updiff="+updiff+"&flag="+flag+"&imagefile="+baseImageFile;
				$("#newimg").attr("src",srcurl);
			});
			
			//重置
			$("#reset").click(function(){
				var baseImageFile = "/statics/sourceimage/floodfill3.png";
				$("#oldimg").attr("src",baseUrl+baseImageFile);
				$("#newimg").attr("src",'');
				layer.msg('重置成功!', {icon: 1});
			});
			
			//滑动插件加载
			$("#graysize").slider({
				tooltip: 'always',
			});
			$("#lodiff").slider({
				tooltip: 'always',
			});
			$("#updiff").slider({
				tooltip: 'always',
			});
			$("#flag").slider({
				tooltip: 'always',
			});
			
 			$("#graysize").on("slide", function(slideEvt) {
				console.log(slideEvt.value);
				//$("#binary").click();
			}).on("change", function (e) {  
			    //当值发生改变的时候触发  
			    //console.info(e);  
			    //获取旧值和新值  
			    console.info(e.value.oldValue + '--' + e.value.newValue);
				$("#graysize").val(e.value.newValue);
			    $("#floodfill").click();
			});
 			
 			$("#lodiff").on("slide", function(slideEvt) {
				console.log(slideEvt.value);
				//$("#binary").click();
			}).on("change", function (e) {  
			    //当值发生改变的时候触发  
			    //console.info(e);  
			    //获取旧值和新值  
			    console.info(e.value.oldValue + '--' + e.value.newValue);
				$("#lodiff").val(e.value.newValue);
			    $("#floodfill").click();
			});
 			
 			$("#updiff").on("slide", function(slideEvt) {
				console.log(slideEvt.value);
				//$("#binary").click();
			}).on("change", function (e) {  
			    //当值发生改变的时候触发  
			    //console.info(e);  
			    //获取旧值和新值  
			    console.info(e.value.oldValue + '--' + e.value.newValue);
				$("#updiff").val(e.value.newValue);
			    $("#floodfill").click();
			});
 			
 			$("#flag").on("slide", function(slideEvt) {
				console.log(slideEvt.value);
				//$("#binary").click();
			}).on("change", function (e) {  
			    //当值发生改变的时候触发  
			    //console.info(e);  
			    //获取旧值和新值  
			    console.info(e.value.oldValue + '--' + e.value.newValue);
				$("#flag").val(e.value.newValue);
			    $("#floodfill").click();
			});
		});
		
		
		
	</script>
</head>
<body>

              <div class="box-group" id="accordion">
                <!-- we are adding the .panel class so bootstrap.js collapse plugin detects it -->
                <div class="panel box box-primary">
                  <div class="box-header with-border">
                    <h4 class="box-title">
                      <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">
                       addweighted
                      </a>
                    </h4>
                  </div>
                  <div id="collapseOne" class="panel-collapse collapse"><!--class="panel-collapse collapse in"中的 in 控制展开 -->
                    <div class="box-body">
                      		<h4>参考资料：<br>
                      		<a href="https://docs.opencv.org/2.4.13.6/modules/core/doc/operations_on_arrays.html#addweighted">Opencv官方文档：addweighted</a> <br>
                      		<br>锐化：dst (I)= saturate ( src1(I)* lodiff +  src2(I)* updiff +  flag )
                      		 public static void addWeighted(Mat src1, double lodiff, Mat src2, double updiff, double flag, Mat dst); 各参数说明：</h4>
                      		<table class="table table-bordered">
				                <tbody><tr>
				                  <th style="width: 10px">#</th>
				                  <th style="width: 20%">参数</th>
				                  <th>说明</th>
				                </tr>
				                <tr>
				                  <td>1.</td>
				                  <td>Mat src1</td>
				                  <td>图层1对应的Mat对象</td>
				                </tr>
				                <tr>
				                  <td>2.</td>
				                  <td>double lodiff</td>
				                  <td>图层1的透明度权重</td>
				                </tr>
				                <tr>
				                  <td>3.</td>
				                  <td>Mat src2</td>
				                  <td>图层2对应的Mat对象</td>
				                </tr>
				                <tr>
				                  <td>4.</td>
				                  <td>double updiff</td>
				                  <td>图层2的透明度权重</td>
				                </tr>
				                <tr>
				                  <td>5.</td>
				                  <td>double flag</td>
				                  <td>一个加到权重总和上的标量值，越大合并图像越明亮</td>
				                </tr>
				                <tr>
				                  <td>6.</td>
				                  <td>Mat dst</td>
				                  <td>目标图像mat</td>
				                </tr>
				              </tbody></table>
              
              
                      </div>
                  </div>
                </div>
            </div>
            <!-- /.box-body -->
            
            <h4>&nbsp;&nbsp;漫水填充测试</h4>	
    			 <div class="box-body">
    			<table class="table table-bordered">
                <tbody><tr>
                  <th style="width:20%">灰度值</th>
                  <th style="width:20%">lodiff</th>
                  <th style="width:20%">updiff</th>
                  <th style="width:20%">flag</th>
                  <th style="width: 200px">操作</th>
                </tr>
                <tr>
                  <td>
                  		<input id="graysize" data-slider-id='ex1Slider' type="text" data-slider-min="0" data-slider-max="255" data-slider-step="1" data-slider-value="40"/>
				  </td>
                  <td>
                  		<input id="lodiff" data-slider-id='ex1Slider' type="text" data-slider-min="0" data-slider-max="100" data-slider-step="1" data-slider-value="5"/>
                  </td>
                  <td>
                  		<input id="updiff" data-slider-id='ex1Slider' type="text" data-slider-min="0" data-slider-max="100" data-slider-step="1" data-slider-value="5"/>
                  </td>
                  <td>
                  		<input id="flag" data-slider-id='ex1Slider' type="text" data-slider-min="0" data-slider-max="131072" data-slider-step="1" data-slider-value="131072"/>
                  </td>
                  <td><a class="btn btn-info"  id="floodfill"><i class="fa fa-object-ungroup"></i>漫水填充</a>
			          <a class="btn btn-info" id="reset"><i class="fa fa-refresh"></i>重置</a></td>
                </tr>
              </tbody></table></div>
    			
    			
	
			   
			<div class="row">
			    <div class="col-sm-6">
			      <div class="box box-primary">
			        <div class="box-header with-border">
			          <h3 class="box-title">原图</h3>
			          <span class="label label-primary pull-right"><i class="fa fa-html5"></i></span>
			        </div><!-- /.box-header -->
			        <div class="box-body">
			          <p>未识别前的原文件。</p>
		          		<img id="oldimg" src=""  alt="原图" />
			        </div><!-- /.box-body -->
			      </div><!-- /.box -->
			    </div><!-- /.col -->
			    <div class="col-sm-6">
			      <div class="box box-danger">
			        <div class="box-header with-border">
			          <h3 class="box-title">识别后的图片</h3>
			          <span class="label label-danger pull-right"><i class="fa fa-database"></i></span>
			        </div><!-- /.box-header -->
			        <div class="box-body">
			          <p>点击识别按钮后，将显示识别后的文件。</p>
			          <img  id="newimg" src=""  alt="识别后的图" />

			        </div><!-- /.box-body -->
			      </div><!-- /.box -->
			    </div><!-- /.col -->
			  </div>
						
			
</body>
<script src="${ctxStatic}/plugins/bootstrap-slider/bootstrap-slider.js?t=${version}"></script>
</html>
