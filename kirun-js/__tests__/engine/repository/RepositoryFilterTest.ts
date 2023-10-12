import { KIRunSchemaRepository, KIRunFunctionRepository } from '../../../src';

const funcRepo = new KIRunFunctionRepository();
const schemaRepo = new KIRunSchemaRepository();

test('Repository Filter Test', async () => {
    expect(await funcRepo.filter('Rep')).toStrictEqual([
        'System.String.Repeat',
        'System.String.Replace',
        'System.String.ReplaceFirst',
        'System.String.PrePad',
        'System.String.ReplaceAtGivenPosition',
    ]);

    expect(await funcRepo.filter('root')).toStrictEqual([
        'System.Math.CubeRoot',
        'System.Math.SquareRoot',
    ]);

    expect(await schemaRepo.filter('root')).toStrictEqual([]);

    expect(await schemaRepo.filter('rin')).toStrictEqual(['System.string']);

    expect(await schemaRepo.filter('ny')).toStrictEqual(['System.any']);

    expect(await schemaRepo.filter('')).toStrictEqual([
        'System.any',
        'System.boolean',
        'System.double',
        'System.float',
        'System.integer',
        'System.long',
        'System.number',
        'System.string',
        'System.ParameterExpression',
        'System.Null',
        'System.Schema',
    ]);
});
