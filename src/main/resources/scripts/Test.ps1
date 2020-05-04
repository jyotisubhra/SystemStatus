$file = "c:\setupisam.log"
$file1 = "c:\SUService.log"
$command = Invoke-Command -scriptblock {param($file)(Test-Path $file)} -ArgumentList $file
$command1 = Invoke-Command -scriptblock {param($file)(Get-ChildItem $file).length/1kb} -ArgumentList $file
$size = [math]::ceiling($command1)
$creationTime = Invoke-Command -scriptblock {param($file)(Get-item $file).creationtime} -ArgumentList $file

Write-Host "output is: $command - $size - $creationTime" 