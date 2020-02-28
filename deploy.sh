echo "===========进入git项目happymmall目录============="
cd /root/mmallx
echo "==========git切换分之到mmall-v1.0==============="
git checkout master
echo "==================git fetch======================"
git fetch
echo "==================git pull======================"
git pull
echo "===========编译并跳过单元测试===================="
mvn clean package -Dmaven.test.skip=true
echo "============删除旧的ROOT.war==================="
rm /webapps/tomcat-7/webapps/ROOT.war
echo "======拷贝编译出来的war包到tomcat下-ROOT.war======="
cp /root/mmallx/target/mmall.war /webapps/tomcat-7/webapps/ROOT.war
echo "============删除tomcat下旧的ROOT文件夹============="
rm -rf /webapps/tomcat-7/webapps/ROOT
echo "====================关闭tomcat====================="
/webapps/tomcat-7/bin/shutdown.sh

# sleep 10s 等待spring项目关闭
# 如果项目比较大那么需要sleep 更多的时间
echo "================sleep 10s========================="
for i in {1..10}
do
    echo $i"s"
    sleep 1s
done
echo "====================启动tomcat====================="
/webapps/tomcat-7/bin/startup.sh
