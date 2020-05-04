### Functions ###

	function get-filename ([string]$FolderLocation, [string]$FileName) {
		$file = "$FolderLocation\$FileName"
	    return $file
	}

	function get-size ([string]$file) {
		$command = Invoke-Command -scriptblock {param($file)(Get-ChildItem $file).length/1kb} -ArgumentList $file
		$size = [math]::ceiling($command)
	    return $size
	}

	function get-creationTime ([string]$file) {
		$creationTime = Invoke-Command -scriptblock {param($file)(Get-item $file).creationtime} -ArgumentList $file
	    return $creationTime
	}
	
	function isOldCreationDate ($creationTime) {
	    $creationDate = $creationTime.ToString("yyyyMMdd")
	    $currentDate = Get-Date | Select-Object Date | Format-Table -HideTableHeaders
	    if ($creationDate -lt $currentDate) {
	    	return 1
		} else {
	    	return 0
		}
	}

	function get-noOfLines ([string]$file) {
	    $nlines = 0;
	    Invoke-Command -scriptblock {param($file)(gc $file -read 1000 | % { $nlines += $_.Length })} -ArgumentList $file
		return $nlines
	}
	
	function get-LogFile($inputFile, $destinationRoot) {
		$nameWithoutExt = (Get-Item $inputFile ).Basename
		$logFile = $destinationRoot + "\"  + $nameWithoutExt + ".log"
		return $logFile
	}

	function processIndividualFile($inputFullPath, $logFile) {
		
		foreach($line in [System.IO.File]::ReadLines($inputFullPath)) {
	
		$lines = $line -split ','
		$ID = $lines[0]
		$FolderLocation = $lines[1]
		$FileName = $lines[2]
		
		$size = "0"
		$FileStatus = "AVL"
		$noOfLines = "0"
		$date = Get-Date -Format "yyyyMMdd"
	    $time = Get-Date -Format "HH:mm:ss"
		
		$file = get-filename $lines[1] $lines[2]
	    Write-Host "Filename is: $file"
	    
	    
	    $command = Invoke-Command -scriptblock {param($file)(Test-Path $file)} -ArgumentList $file
	    if (!$command) { 
	    	Write-Warning "The file $filename is NOT available in the Locations"     	
	    	$FileStatus = "NAVL"   	
	  	} else {
	    	Write-Host "The file $filename is available in the Locations"     	
	    	$creationTime = get-creationTime "$file"
	    	if (isOldCreationDate ($creationTime)) {
	    		$FileStatus = "NAVL"
	    	} else {
	    		Write-Host 'The creationDate date is earlier than the current date'
	    		$date = $creationTime.ToString("yyyyMMdd")
	    		$time = $creationTime.ToString("HH:mm:ss")
	    		$noOfLines = get-noOfLines "$file"
	    		$size = get-size "$file"
	    	}
	    	
	    	Write-Host "File Size: $size, No Of Lines : $noOfLines, date: $date, Time: $time, FileStatus: $FileStatus"
	    }  
	    $outputValue = $lines[0] + "|"  + $FolderLocation + "|" + $FileName + "|"  + $date + "|" + $time + "|" + $size + "|"  + $noOfLines + "|" + $FileStatus
	    Write-Host "output is: $outputValue" 
	    Add-Content -Value $outputValue -Path $logFile
		}
	}

### Start of process ###


	$inputText=$args[0]
	
	$allInputs = $inputText -split ','
	$sourceRoot=$allInputs[1]
	$destinationRoot=$allInputs[2]
	
	Write-Host $allInputs[0]
	Write-Host $sourceRoot
	Write-Host $destinationRoot
	New-Item -ItemType Directory -Force -Path $destinationRoot
	
	$files = Get-ChildItem $sourceRoot -Filter *.properties
	foreach ($inputFile in $files) {
		$inputFullPath = $inputFile.FullName
		Write-Host "Iterate Filename is: $inputFullPath"
		
		$logFile = get-LogFile $inputFullPath $destinationRoot
		Write-Host "logFile name: $logFile"
		New-Item $logFile -ItemType File
		
		$outputValue = processIndividualFile $inputFullPath $logFile
		
	}



