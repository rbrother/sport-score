npm install
Remove-Item -Path ".\resources\public\js\compiled" -Recurse
npm run release
aws s3 cp .\resources\public  s3://sport-tracker/ --recursive
