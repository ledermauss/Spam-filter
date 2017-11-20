# use: ./script [small] threshold1 threshold2...

args=($*)
file='datasets/nbfh.buckets.csv'
origin='../output/out.nbfh.csv'
command='nbfh_buckets'
echo '\c'  > $file

if [ $1 == "small" ] # for using the small dataset
then
        command='nbfh_buckets_small'
        origin='../output/small.nbfh.csv'
        shift
fi


for param in $*;
do
        make -C ../ LOG_BUCKETS_TEST=$param $command
        tail -n +2 $origin >> $file
done
head -n 1 $origin | cat - $file > temp && mv temp $file
