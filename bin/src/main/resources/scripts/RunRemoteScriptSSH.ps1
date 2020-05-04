$servername=$args[0]

$arr = $servername -split ','
$sourceRoot=$arr[1]
$destinationRoot=$arr[2]

write-output $arr[0]
write-output $sourceRoot
write-output $destinationRoot

New-Item -Path $destinationRoot -ItemType Directory
Copy-Item -Path $sourceRoot -Recurse -Destination $destinationRoot -Container
Get-ChildItem -Path $destinationRoot
