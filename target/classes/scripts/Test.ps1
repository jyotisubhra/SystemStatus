$file = "c:\setupisam.log"
$command = Invoke-Command -scriptblock {param($file)(Test-Path $file)} -ArgumentList $file
$command1 = Invoke-Command -scriptblock {param($file)(Get-ChildItem $file).length/1kb} -ArgumentList $file
$size = [math]::ceiling($command1)
$creationTime = Invoke-Command -scriptblock {param($file)(Get-item $file).creationtime} -ArgumentList $file

Write-Host "output is: $command - $size - $creationTime" 

$previousDate = (get-date).AddDays(-1)
$date = [DateTime]::Today.AddDays(-1).ToString("yyyyMMdd")
Write-Host "$date"

$t = New-Object Net.Sockets.TcpClient "google.com", 443
	    if($t.Connected) {
	        Write-Host "connected"
	    } else {
	    	Write-Host "not connected"
	    }