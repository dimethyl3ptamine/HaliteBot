set bot1=..\out
set bot2=bots\Bot_27
set bot3=bots\Bot_28
set bot4=bots\Bot_31

halite -d "240 160" "java -cp %bot1% MyBot" "java -cp %bot2% MyBot" "java -cp %bot3% MyBot" "java -cp %bot4% MyBot"
