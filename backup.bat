set zip="C:\Program Files\7-Zip\7z.exe"

REM ====== Read version file ======
set /p version=< .\version

REM ====== Get proper date: yyyyMMdd_hh_mm_ss ======
set today=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%_%time:~3,2%_%time:~6,2%
set today=%today: =%

REM ====== Pack into zip ======
set backupFilename=dmt_%version%_%today%.zip
%zip% a -tzip ".\zip\"%backupFilename% .\src\*