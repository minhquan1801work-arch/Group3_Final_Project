@echo off
cd /d C:\Users\LENOVO\AndroidStudioProjects\Group3_Final_Project

echo === Khoi tao Git repo ===
git init

echo === Them .gitignore cho Android ===
echo *.iml > .gitignore
echo .gradle >> .gitignore
echo /local.properties >> .gitignore
echo /.idea >> .gitignore
echo .DS_Store >> .gitignore
echo /build >> .gitignore
echo /captures >> .gitignore
echo .externalNativeBuild >> .gitignore
echo .cxx >> .gitignore
echo local.properties >> .gitignore
echo service-account.json >> .gitignore

echo === Ket noi GitHub remote ===
git remote remove origin 2>nul
git remote add origin https://github.com/minhquan1801work-arch/Group3_Final_Project.git

echo === Tao branch feature/person-a ===
git checkout -b feature/person-a 2>nul || git checkout feature/person-a

echo === Stage tat ca file ===
git add .

echo === Commit ===
git commit -m "feat: initial commit - Glassity base project + firebase + 20 products"

echo === Push len GitHub ===
git push -u origin feature/person-a

echo.
echo === XONG! Kiem tra GitHub de xac nhan ===
pause
