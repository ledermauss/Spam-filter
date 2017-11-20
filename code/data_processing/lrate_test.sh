# use: ./script [small] threshold1 threshold2...

args=($*)
file='datasets/pfh.lrate.csv'
origin='../output/out.nbfh.csv'
command='pfh_lrate'
echo '\c'  > $file

if [ $1 == "small" ] # for using the small dataset
then
        command='pfh_lrate_small'
        origin='../output/small.pfh.csv'
        shift
fi


for param in $*;
do
        make -C ../ LEARNING_RATE_TEST=$param $command
        tail -n +2 $origin >> $file
done
head -n 1 $origin | cat - $file > temp && mv temp $file
