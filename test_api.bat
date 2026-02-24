@echo off
echo Testing User Registration API...
curl -X POST "http://localhost:8080/user/register" -H "Content-Type: application/json" -d "{\"username\":\"testuser3\",\"password\":\"123456\",\"email\":\"test3@example.com\",\"phone\":\"13800138002\",\"nickname\":\"测试用户3\"}"
echo.
echo.
echo Testing User Login API...
curl -X POST "http://localhost:8080/user/login" -H "Content-Type: application/json" -d "{\"username\":\"testuser3\",\"password\":\"123456\"}"
echo.
pause
