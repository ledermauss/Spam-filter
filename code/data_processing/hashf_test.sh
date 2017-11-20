# use: ./script [small] threshold1 threshold2...

args=($*)
file='datasets/pfh.hashf.csv'
origin='../output/out.pcms.csv'
command='pcms_hashf'
echo '\c'  > $file

if [ $1 == "small" ] # for using the small dataset
then
        command='pcms_hashf_small'
        origin='../output/small.pcms.csv'
        shift
fi


for param in $*;
do
        make -C ../ NB_HASHES_TEST=$param $command
        tail -n +2 $origin >> $file
done
head -n 1 $origin | cat - $file > temp && mv temp $file
