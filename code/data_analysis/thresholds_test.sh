# use: ./script [small] threshold1 threshold2...

args=($*)
file='datasets/nbfh.threshold.csv'
origin='../output/out.nbfh.csv'
command='nbfh_threshold'
echo '\c'  > $file

if [ $1 == "small" ] # for using the small dataset
then
        command='nbfh_threshold_small'
        origin='../output/small.nbfh.csv'
        shift
fi


for th in $*;
do
        make -C ../ THRESHOLD_TEST=$th $command
        tail -n +2 $origin >> $file
done
head -n 1 $origin | cat - $file > temp && mv temp $file
