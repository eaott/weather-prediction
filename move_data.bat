cd C:\Users\Evan\GitProjects\weather-prediction
git checkout code
git add --all .
git commit -m "temp to save code state"
git checkout master
git pull
cd C:\Users\Evan\GitProjects\weather-prediction\data
move * C:\Users\Evan\Dropbox\Thesis_Data
cd ..
git add --all .
git commit -m "moved data to Dropbox"
git push
git checkout code